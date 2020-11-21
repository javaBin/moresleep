package no.java.moresleep

import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.PojoMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private class MyDbConnection(val connection: Connection):DbConnection {

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

object ServiceExecutor {
    private val connectionsUsed:ConcurrentHashMap<Long,MyDbConnection> = ConcurrentHashMap()

    fun doStuff(httpMethod: HttpMethod,request:HttpServletRequest,response: HttpServletResponse,doExecutor:(Command,UserType,Map<String,String>)->ServiceResult = {
        command,usertype,parameters ->
        createConnection().use {
            val res = command.execute(usertype, parameters)
            commit()
            res
        }
    }) {
        val pathinfo:String = request.pathInfo!!
        val pathMap = httpMethod.commandFromPathInfo(pathinfo)!!

        val payload:JsonObject = when (httpMethod) {
            HttpMethod.GET,HttpMethod.DELETE -> JsonObject()
            HttpMethod.POST,HttpMethod.PUT -> request.reader.use {  JsonObject.read(it) }
        }

        val command:Command = try {
            PojoMapper.map(payload, pathMap.commandClass.java)
        } catch (e:Exception) {
            throw BadRequest("Invalid input")
        }


        val result:ServiceResult = try {
            doExecutor(command,UserType.ANONYMOUS,pathMap.parameters)
        } catch (reqestError:RequestError) {
            response.sendError(reqestError.httpError,reqestError.errormessage)
            return
        }

        response.contentType = "application/json"
        response.writer.append(result.asJsonObject().toJson())
    }

    @Synchronized fun createConnection():DbConnection {
        val threadId = Thread.currentThread().id
        connectionsUsed[threadId]?.let { throw MoresleepInternalError("Transaction error. Connection already open") }
        val connection = Database.connection()
        connection.autoCommit = false
        val dbConnection = MyDbConnection(connection)
        connectionsUsed[threadId] = dbConnection
        return dbConnection
    }

    fun connection():DbConnection = connectionsUsed[Thread.currentThread().id]?:throw MoresleepInternalError("No found connection")

    fun commit() {
        connectionsUsed[Thread.currentThread().id]?.connection?.commit()
    }

    fun closeConnection() {
        val threadId = Thread.currentThread().id
        val dbconnection = connectionsUsed.remove(threadId)?:throw MoresleepInternalError("Transaction Connection not found")
        dbconnection.connection.rollback()
        dbconnection.connection.close()
    }
}