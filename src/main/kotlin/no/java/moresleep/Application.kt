package no.java.moresleep.java.moresleep

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.webapp.WebAppContext


fun main(args: Array<String>) {
    setupAndStartServer()
    println("Hoyyoy")
}

private fun setupAndStartServer() {
    val server = Server(8080)

    server.handler = createHandler()
    server.start()
}

private fun createHandler(): WebAppContext {
    val webAppContext = WebAppContext()
    webAppContext.initParams["org.eclipse.jetty.servlet.Default.useFileMappedBuffer"] = "false"
    //webAppContext.sessionHandler.setMaxInactiveInterval(30)
    webAppContext.contextPath = "/"

    if (false) {
        // Prod ie running from jar
        webAppContext.baseResource = Resource.newClassPathResource("webapp", true, false)
    } else {
        // Development ie running in ide
        webAppContext.resourceBase = "src/main/resources/webapp"
    }

    webAppContext.addServlet(ServletHolder(ApiServlet()), "/data/*")

    //if (Setup.forceServerHttps()) {
    //    webAppContext.addFilter(FilterHolder(AddStrictSecurityHeaderFilter()), "*", EnumSet.of(DispatcherType.REQUEST))
    //}
    return webAppContext;
}

