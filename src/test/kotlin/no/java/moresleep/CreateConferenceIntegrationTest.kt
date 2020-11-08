package no.java.moresleep

import org.assertj.core.api.Assertions
import org.jsonbuddy.JsonObject
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.BufferedReader
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CreateConferenceIntegrationTest:BaseTestClass() {
    @Test
    fun errorhandlingCreateConference() {
        val createPayLoad = JsonObject().put("slug","javazone2019").toJson()
        val request = Mockito.mock(HttpServletRequest::class.java)

        Mockito.`when`(request.pathInfo).thenReturn("/conference")
        val inputReader = BufferedReader(createPayLoad.reader())
        Mockito.`when`(request.reader).thenReturn(inputReader)


        val response = Mockito.mock(HttpServletResponse::class.java)

        ServiceExecutor.doStuff(HttpMethod.POST,request,response){ command, usertype, pathinfo ->
            command.execute(usertype, pathinfo)
        }


        Mockito.verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,"Missing required value name")

    }

    @Test
    fun createAndReadConference() {
        val conferenceid = createConference()

        println(conferenceid)

    }

    private fun readConferences(conferenceid:String) {
        val request = Mockito.mock(HttpServletRequest::class.java)

        Mockito.`when`(request.pathInfo).thenReturn("/conference")

        val response = Mockito.mock(HttpServletResponse::class.java)
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        Mockito.`when`(response.writer).thenReturn(printWriter)


        ServiceExecutor.doStuff(HttpMethod.GET, request, response) { command, usertype, pathinfo ->
            command.execute(usertype, pathinfo)
        }

        val resultObject = JsonObject.parse(writer.toString())
        Assertions.assertThat(resultObject.toJson()).contains(conferenceid)


    }

    private fun createConference(): String? {
        val createPayLoad = JsonObject().put("name", "Javazone 2021").put("slug", "javazone2021").toJson()
        val request = Mockito.mock(HttpServletRequest::class.java)

        Mockito.`when`(request.pathInfo).thenReturn("/conference")
        val inputReader = BufferedReader(createPayLoad.reader())
        Mockito.`when`(request.reader).thenReturn(inputReader)


        val response = Mockito.mock(HttpServletResponse::class.java)
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        Mockito.`when`(response.writer).thenReturn(printWriter)


        ServiceExecutor.doStuff(HttpMethod.POST, request, response) { command, usertype, pathinfo ->
            command.execute(usertype, pathinfo)
        }

        val resultObject = JsonObject.parse(writer.toString())

        val conferenceid = resultObject.requiredString("id")
        return conferenceid
    }
}