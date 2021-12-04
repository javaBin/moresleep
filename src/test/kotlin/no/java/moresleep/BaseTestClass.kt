package no.java.moresleep

import no.java.moresleep.conference.ConferenceRepo
import no.java.moresleep.talk.DataValue
import no.java.moresleep.talk.PublicTalkReadService
import org.flywaydb.core.Flyway
import org.jsonbuddy.JsonObject
import org.jsonbuddy.JsonString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import java.io.BufferedReader
import java.io.PrintWriter
import java.io.StringWriter
import java.sql.Connection
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun doCommandForTest(pathInfo:String,httpMethod:HttpMethod,createPayload:String?=null,baseUrl:String="/data"):JsonObject {
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



    ServiceExecutor.doStuff(baseUrl,httpMethod, request, response) { command, usertype, pathinfo ->
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

private val flywaySchemaHistorySql = """
     create table flyway_schema_history
        (
        installed_rank integer,        
        version text,
        description text,
        type text,
        script text,
        checksum integer,
        installed_by text,
        installed_on timestamp default now(),
        execution_time integer,
        success boolean
        )
""".trimIndent()

abstract class BaseTestClass {

    val testFullAccessUser:SystemUser = SystemUser(UserType.FULLACCESS,SystemId.UNKNOWN)
    val testAnonUser = SystemUser(UserType.ANONYMOUS,SystemId.ANONYMOUS)

    @BeforeEach
    fun setup() {
        Setup.isRunningJunit = true;
        PublicTalkReadService.allConferences = { ConferenceRepo.allConferences() }
        PublicTalkReadService.clearCache()


        val dataBaseType:DataBaseType = DataBaseType.POSTGRES

        Setup.setValue(SetupValue.DATABASE_TYPE,dataBaseType.name)
        Setup.setValue(SetupValue.DBUSER,if (dataBaseType == DataBaseType.POSTGRES) "localdevuser" else "")
        Setup.setValue(SetupValue.DBPASSWORD,if (dataBaseType == DataBaseType.POSTGRES) "localdevuser" else "")
        Setup.setValue(SetupValue.DATASOURCENAME,"moresleepunit")


        val setup:Pair<((Flyway) -> Unit)?,((Connection)->Unit)?> = when(dataBaseType) {
            DataBaseType.POSTGRES -> Pair({it.clean()},null)
            DataBaseType.SQLLITE -> Pair({it.clean()},null)
            DataBaseType.PGINMEM -> Pair(null,{
                it.createStatement().execute(flywaySchemaHistorySql)
            })
        }

        Database.migrateWithFlyway(setup.first,setup.second)



        Database.migrateWithFlyway(null)
        ServiceExecutor.createConnection()
    }

    @AfterEach
    fun teardown() {
        ServiceExecutor.closeConnection()
        PublicTalkReadService.clearCache()
    }
}