package no.java.moresleep

import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.PojoMapper
import java.io.UnsupportedEncodingException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private class MyDbConnection constructor(val connection: Connection):DbConnection {

    override fun <T> preparedStatement(sql: String, dbcommand: (PreparedStatement) -> T): T {
        return connection.prepareStatement(sql).use(dbcommand)
    }

    override fun <T> allFromQuery(sql: String, dbcommand: (ResultSet) -> T): List<T> {
        return connection.prepareStatement(sql).use { statement ->
            statement.executeQuery().use { rs ->
                val result:MutableList<T> = mutableListOf()
                while (rs.next()) {
                    result.add(dbcommand(rs))
                }
                result
            }
        }
    }

    override fun close() {
        ServiceExecutor.closeConnection()
    }

}

private class Credentials(private val login:String,private val password:String) {
    fun matches(userPasswordString:String):Boolean {
        return (userPasswordString == "$login:$password")
    }
}

private fun credentialsWithBasicAuthentication(req: HttpServletRequest):Credentials?  {
    val authHeader: String = req.getHeader("Authorization")?:return null
    val st = StringTokenizer(authHeader)
    if (st.hasMoreTokens()) {
        val basic = st.nextToken()
        if (!basic.equals("Basic", ignoreCase = true)) {
            return null
        }
        return try {
            val encoded = st.nextToken()
            val credentials:String = String(Base64.getDecoder().decode(encoded))
            val pos:Int = credentials.indexOf(":")
            if (pos != -1) {
                val login = credentials.substring(0, pos).trim()
                val password = credentials.substring(pos + 1).trim()
                Credentials(
                    login,
                    password
                )
            } else {
                null
            }
        } catch (e: UnsupportedEncodingException) {
            null
        }

    }
    return null

}

object ServiceExecutor {
    private val connectionsUsed:ConcurrentHashMap<Long,MyDbConnection> = ConcurrentHashMap()

    private val definedUsers:List<SystemUser> by lazy {
        val basicAuthAccessDev = Setup.readValue(SetupValue.ALLACCESS_USER)
        if (basicAuthAccessDev.indexOf("=") == -1) {
            listOf(SystemUser(UserType.FULLACCESS, SystemId.UNKNOWN, basicAuthAccessDev))
        } else {
            val result:MutableList<SystemUser> = mutableListOf()
            var currindex=0
            while (true) {
                val eqpos = basicAuthAccessDev.indexOf("=",currindex)
                if (eqpos == -1) {
                    break
                }
                val systemId:SystemId = SystemId.valueOf(basicAuthAccessDev.substring(currindex,eqpos))

                val nextpos = basicAuthAccessDev.indexOf(",",currindex)
                val hasMore = (nextpos >= 0)
                val accessString = if (hasMore) basicAuthAccessDev.substring(eqpos+1,nextpos) else basicAuthAccessDev.substring(eqpos+1)
                result.add(SystemUser(UserType.FULLACCESS,systemId,accessString))
                if (!hasMore) {
                    break
                }
                currindex = nextpos+1
            }
            result
        }
    }

    private fun userFromCredentials(credetials:Credentials?):SystemUser {
        if (Setup.readBoolValue(SetupValue.ALL_OPEN_MODE)) {
            return SystemUser(UserType.FULLACCESS,SystemId.MORESLEEP_ADMIN)
        }
        if (credetials == null) {
            return SystemUser(UserType.ANONYMOUS,SystemId.ANONYMOUS)
        }
        if (credetials.matches(Setup.readValue(SetupValue.READ_USER))) {
            return SystemUser( UserType.READ_ONLY,SystemId.READ_ONLY_SYSTEM)
        }

        for (systemUser in definedUsers) {
            if (systemUser.basicAuthAccessDev != null && credetials.matches(systemUser.basicAuthAccessDev)) {
                return systemUser
            }
        }
        throw ForbiddenRequest("Unknown authorization")

    }

    fun doStuff(baseUrl:String,httpMethod: HttpMethod,request:HttpServletRequest,response: HttpServletResponse,
                doExecutor:(Command,SystemUser,Map<String,String>)->ServiceResult = {
                    command,usertype,parameters ->
                    createConnection().use {
                        val res = command.execute(usertype, parameters)
                        commit()
                        res
                    }
    }) {
        val pathinfo:String = baseUrl + (request.pathInfo?:"")
        val additionalParas:Map<String,String> = request.getHeader("If-Unmodified-Since")?.let {
            mapOf(Pair("If-Unmodified-Since",it))
        }?: emptyMap()
        val pathMap = httpMethod.commandFromPathInfo(pathinfo,additionalParas)?:throw BadRequest("Unknown path $pathinfo")

        val payload:JsonObject = when (httpMethod) {
            HttpMethod.GET,HttpMethod.DELETE -> JsonObject()
            HttpMethod.POST,HttpMethod.PUT -> request.reader.use {  JsonObject.read(it) }
        }

        if (payload.containsKey("requiredAccess")) {
            throw ForbiddenRequest("No requiredAccess")
        }



        val command:Command = try {
            PojoMapper.map(payload, pathMap.commandClass.java)
        } catch (e:Exception) {
            throw BadRequest("Invalid input")
        }

        val credentials = credentialsWithBasicAuthentication(request)
        val systemUser:SystemUser = userFromCredentials(credentials)


        val result:ServiceResult = try {
            if (systemUser.userType < command.requiredAccess) {
                if (systemUser.userType == UserType.ANONYMOUS) {
                    throw RequestError(HttpServletResponse.SC_UNAUTHORIZED,"Unauthorized")
                } else {
                    throw ForbiddenRequest("Not required access")
                }
            }

            doExecutor(command,systemUser,pathMap.parameters)
        } catch (reqestError:RequestError) {
            response.sendError(reqestError.httpError,reqestError.errormessage)
            return
        }


        if (command is AllowAllOrigins) {
            response.addHeader("Access-Control-Allow-Origin","*")
        }

        response.contentType = "application/json"
        response.writer.append(result.asJsonObject().toJson())
    }

    @Synchronized fun createConnection(connectionCreator:(()->Connection)={
        val connection = Database.connection()
        connection.autoCommit = false
        connection
    }):DbConnection {
        val threadId = Thread.currentThread().id
        connectionsUsed[threadId]?.let { throw MoresleepInternalError("Transaction error. Connection already open") }
        val connection = connectionCreator.invoke()
        val dbConnection = MyDbConnection(connection)
        connectionsUsed[threadId] = dbConnection
        return dbConnection
    }



    fun connection():DbConnection = connectionsUsed[Thread.currentThread().id]?:throw MoresleepInternalError("No found connection")

    fun hasConnection():Boolean = (connectionsUsed[Thread.currentThread().id] != null)

    fun commit() {
        connectionsUsed[Thread.currentThread().id]?.connection?.commit()
    }

    fun rollback() {
        connectionsUsed[Thread.currentThread().id]?.connection?.rollback()
    }

    fun removeConnection() {
        val threadId = Thread.currentThread().id
        val dbconnection = connectionsUsed.remove(threadId)?:throw MoresleepInternalError("Transaction Connection not found")
        dbconnection.connection.rollback()
    }

    fun closeConnection() {
        val threadId = Thread.currentThread().id
        val dbconnection = connectionsUsed.remove(threadId)?:throw MoresleepInternalError("Transaction Connection not found")
        dbconnection.connection.rollback()
        dbconnection.connection.close()
    }
}