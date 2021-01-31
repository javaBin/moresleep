package no.java.moresleep

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

enum class SetupValue(val defaultValue:String) {
    SERVER_PORT("8082"),
    RUN_FROM_JAR("false"),
    DBHOST("localhost"),
    DBPORT("5432"),
    DATASOURCENAME("moresleeplocal"),
    DBUSER("localdevuser"),
    DBPASSWORD("localdevuser"),
    DATABASE_TYPE("POSTGRES"),
    SLEEPINGPILL_AUTH(""),
    LOAD_FROM_SLEEPINGPILL("false"),
    SLEEPING_PILL_ADDR("https://sleepingpill.javazone.no"),
    ALL_OPEN_MODE("true"),
    ALLACCESS_USER(""),
    READ_USER("")
}

object Setup {
    private val setupvalues:ConcurrentMap<SetupValue,String> = ConcurrentHashMap()

    fun readValue(setupValue: SetupValue):String = setupvalues[setupValue]?:setupValue.defaultValue
    fun readBoolValue(setupValue: SetupValue):Boolean = (readValue(setupValue) == "true")

    fun loadFromFile(args: Array<String>) {
        if (args.size < 1) {
            return
        }
        val setuplines:List<String> = File(args[0]).readLines(Charsets.UTF_8)
        for (line in setuplines) {
            if (line.isBlank() || line.startsWith("#")) {
                continue
            }
            val eqInd = line.indexOf("=")
            if (eqInd == -1) {
                continue
            }
            val setupvalStr = line.substring(0,eqInd)
            val setupKey:SetupValue = SetupValue.valueOf(setupvalStr)
            val setupValue:String = line.substring(eqInd+1)
            setupvalues[setupKey] = setupValue
        }
    }


    fun setValue(setupValue: SetupValue,value:String) {
        setupvalues[setupValue] = value
    }


}