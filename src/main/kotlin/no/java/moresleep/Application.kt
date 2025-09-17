package no.java.moresleep.java.moresleep

import no.java.moresleep.Database
import no.java.moresleep.Setup
import no.java.moresleep.SetupValue
import no.java.moresleep.util.PopulateWorker
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.webapp.WebAppContext



class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            setupAndStartServer(args)
            println("Hoyyoy")
        }
    }
}

private fun setupAndStartServer(args: Array<String>) {
    Setup.loadFromFile(args)
    Database.migrateWithFlyway(null,null)
    PopulateWorker.populateAll()
    val server = Server(Setup.readValue(SetupValue.SERVER_PORT).toInt())

    server.handler = createHandler()
    server.start()
}

private fun createHandler(): Handler {
    val webAppContext = WebAppContext()
    webAppContext.initParams["org.eclipse.jetty.servlet.Default.useFileMappedBuffer"] = "false"
    webAppContext.contextPath = "/"

    if (Setup.readBoolValue(SetupValue.RUN_FROM_JAR)) {
        // Prod ie running from jar
        println("Running from jar")
        webAppContext.baseResource = Resource.newClassPathResource("webapp", true, false)
    } else {
        // Development ie running in ide
        println("Running from local filesystem")
        webAppContext.resourceBase = "src/main/resources/webapp"
    }

    webAppContext.addServlet(ServletHolder(ApiServlet("/data")), "/data/*")
    webAppContext.addServlet(ServletHolder(ApiServlet("/public")), "/public/*")

    val gzipHandler = GzipHandler()
    gzipHandler.handler = webAppContext
    return gzipHandler;
}

