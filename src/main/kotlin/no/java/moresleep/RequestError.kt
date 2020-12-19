package no.java.moresleep

import java.net.http.HttpResponse
import javax.servlet.http.HttpServletResponse

open class RequestError(val httpError:Int,val errormessage:String):RuntimeException()

class BadRequest(errormessage: String):RequestError(HttpServletResponse.SC_BAD_REQUEST,errormessage)

class ForbiddenRequest(errormessage: String):RequestError(HttpServletResponse.SC_FORBIDDEN,errormessage)