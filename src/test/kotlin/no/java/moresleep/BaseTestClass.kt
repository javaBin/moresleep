package no.java.moresleep

import no.java.moresleep.talk.DataValue
import org.jsonbuddy.JsonObject
import org.jsonbuddy.JsonString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import java.io.BufferedReader
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun doCommandForTest(pathInfo:String,httpMethod:HttpMethod,createPayload:String?=null):JsonObject {
    val request = Mockito.mock(HttpServletRequest::class.java)

    Mockito.`when`(request.pathInfo).thenReturn(pathInfo)
    if (createPayload != null) {
        val inputReader = BufferedReader(createPayload.reader())
        Mockito.`when`(request.reader).thenReturn(inputReader)
    }

    val response = Mockito.mock(HttpServletResponse::class.java)
    val writer = StringWriter()
    val printWriter = PrintWriter(writer)
    Mockito.`when`(response.writer).thenReturn(printWriter)
    Mockito.`when`(response.sendError(Mockito.anyInt(),Mockito.anyString())).thenThrow(RuntimeException("Got http error"))



    ServiceExecutor.doStuff(httpMethod, request, response) { command, usertype, pathinfo ->
        command.execute(usertype, pathinfo)
    }


    val resultObject = JsonObject.parse(writer.toString())
    return resultObject
}

val baseTalkDataTestset:Map<String, DataValue> = mapOf(
    Pair("title", DataValue(privateData = false,value = JsonString("My cool talk title"))),
    Pair("abstract", DataValue(privateData = false,value = JsonString("Here is the abstract"))),
    Pair("outline", DataValue(privateData = true,value = JsonString("This is an outline"))),
)

val baseSpeakerDataTestset:Map<String, DataValue> = mapOf(
    Pair("bio", DataValue(privateData = false,value = JsonString("This is a cool speaker"))),
)

abstract class BaseTestClass {
    @BeforeEach
    fun setup() {
        Setup.setValue(SetupValue.DATABASE_TYPE,"SQLLITE")
        //Setup.setValue(SetupValue.DATABASE_TYPE,"PGINMEM")
        Setup.setValue(SetupValue.DBUSER,"")
        Setup.setValue(SetupValue.DBPASSWORD,"")
        Setup.setValue(SetupValue.DATASOURCENAME,"junit")
        Database.migrateWithFlyway({ it.clean() })
        ServiceExecutor.createConnection()
    }

    @AfterEach
    fun teardown() {
        ServiceExecutor.closeConnection()
    }
}