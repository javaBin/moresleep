package no.java.moresleep

import no.java.moresleep.talk.CreateNewSession
import no.java.moresleep.talk.SessionStatus
import no.java.moresleep.talk.SpeakerUpdate
import org.assertj.core.api.Assertions
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.JsonGenerator
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.BufferedReader
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class IntegrationTest:BaseTestClass() {
    @Test
    fun errorhandlingCreateConference() {
        val createPayLoad:String = JsonObject().put("slug","javazone2019").toJson()
        val request = Mockito.mock(HttpServletRequest::class.java)

        Mockito.`when`(request.pathInfo).thenReturn("/conference")
        val inputReader = BufferedReader(createPayLoad.reader())
        Mockito.`when`(request.reader).thenReturn(inputReader)


        val response = Mockito.mock(HttpServletResponse::class.java)

        ServiceExecutor.doStuff("/data",HttpMethod.POST,request,response){ command, usertype, pathinfo ->
            command.execute(usertype, pathinfo)
        }


        Mockito.verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,"Missing required value name")

    }

    @Test
    fun createAndReadConference() {
        val conferenceid = createConference()
        readConferences(conferenceid)
    }

    private fun readConferences(conferenceid:String) {
        val pathInfo:String = "/conference"
        val httpMethod:HttpMethod = HttpMethod.GET

        val resultObject = doCommandForTest(pathInfo,httpMethod)

        Assertions.assertThat(resultObject.toJson()).contains(conferenceid)

    }

    private fun createConference(): String {
        val createPayLoad = JsonObject().put("name", "Javazone 2021").put("slug", "javazone2021").toJson()
        val resultObject = doCommandForTest("/conference",HttpMethod.POST,createPayLoad)

        val conferenceid = resultObject.requiredString("id")
        return conferenceid
    }

    @Test
    fun createReadTalk() {
        val conferenceid = createConference()
        val createNewSession = CreateNewSession(data= baseTalkDataTestset,postedBy = "anders@java.no",status = SessionStatus.SUBMITTED.name,
            speakers = listOf(SpeakerUpdate(name = "Anders Lastname",email = "anders@java.no",data = baseSpeakerDataTestset)))
        val createPayload:JsonObject = JsonGenerator.generate(createNewSession) as JsonObject
        val resultObject = doCommandForTest("/conference/$conferenceid/session",HttpMethod.POST,createPayload.toJson())
        val talkid = resultObject.requiredString("id")

        val readTalk:JsonObject = doCommandForTest("/session/$talkid",HttpMethod.GET)
        Assertions.assertThat(readTalk).isEqualTo(resultObject)
    }
}