package no.java.moresleep.hooks

import org.jsonbuddy.pojo.JsonGenerator
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.*
import kotlin.concurrent.*

class HttpHookReporter(private val url:String): HookReporterInterface {
    override fun reportHook(hookMessage: HookMessage) {
        thread(start = true) {
            val jsonPaylload = JsonGenerator.generate(hookMessage)
            val urlpath = URI(url).toURL()
            val conn = urlpath.openConnection() as HttpURLConnection
            conn.setRequestMethod("POST")
            conn.setDoOutput(true)
            val printWriter = PrintWriter(OutputStreamWriter(conn.outputStream), true)
            printWriter.use {
                jsonPaylload.toJson(it)
            }
        }
    }
}