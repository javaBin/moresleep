package no.java.no.java.moresleep

import java.io.PrintWriter
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ApiServlet:HttpServlet() {

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "application/json"
        val pw = PrintWriter(resp.outputStream).use {
            it.append("""{"greeting":42}""")

        }
    }
}