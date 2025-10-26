package no.java.moresleep.hooks

class DummyHookReporter:HookReporterInterface {
    override fun reportHook(hookMessage: HookMessage) {
        println("Hook reported $hookMessage")
    }
}