package no.java.moresleep.java.moresleep

import no.java.moresleep.HttpMethod
import no.java.moresleep.ServiceExecutor
import java.io.PrintWriter
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ApiServlet:HttpServlet() {

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        ServiceExecutor.doStuff(HttpMethod.GET,req,resp)
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        ServiceExecutor.doStuff(HttpMethod.POST,req,resp)
    }

    override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
        ServiceExecutor.doStuff(HttpMethod.PUT,req,resp)
    }

}