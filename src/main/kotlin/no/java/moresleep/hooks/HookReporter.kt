package no.java.moresleep.hooks

import no.java.moresleep.Setup
import no.java.moresleep.SetupValue

object HookReporter {
    var givenHook: HookReporterInterface? = null

    private val reporter:HookReporterInterface by lazy {
        val url = Setup.readValue(SetupValue.HOOK_WEB_ADDR)
        givenHook ?: if (url.trim().isNotEmpty()) HttpHookReporter(url) else DummyHookReporter()
    }

    fun reportHook(hookMessage: HookMessage) {
        reporter.reportHook(hookMessage)
    }
}