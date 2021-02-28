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

    fun doStuff(baseUrl:String,httpMethod: HttpMethod,request:HttpServletRequest,response: HttpServletResponse,
                doExecutor:(Command,UserType,Map<String,String>)->ServiceResult = {
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
        val userType:UserType = when {
            Setup.readBoolValue(SetupValue.ALL_OPEN_MODE) -> UserType.FULLACCESS
            credentials?.matches(Setup.readValue(SetupValue.ALLACCESS_USER)) == true -> UserType.FULLACCESS
            credentials?.matches(Setup.readValue(SetupValue.READ_USER)) == true -> UserType.READ_ONLY
            credentials != null -> throw ForbiddenRequest("Unknown authorization")
            else -> UserType.ANONYMOUS
        }



        val result:ServiceResult = try {
            if (userType < command.requiredAccess) {
                if (userType == UserType.ANONYMOUS) {
                    throw RequestError(HttpServletResponse.SC_UNAUTHORIZED,"Unauthorized")
                } else {
                    throw ForbiddenRequest("Not required access")
                }
            }

            doExecutor(command,userType,pathMap.parameters)
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