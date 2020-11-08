package no.java.moresleep

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

enum class SetupValue(val defaultValue:String) {
    DBHOST("localhost"),
    DBPORT("5432"),
    DATASOURCENAME("localdevdb"),
    DBUSER("localdevuser"),
    DBPASSWORD("localdevuser"),
}

object Setup {

    private val setupvalues:ConcurrentMap<SetupValue,String> = ConcurrentHashMap()

    fun readValue(setupValue: SetupValue):String = setupvalues[setupValue]?:setupValue.defaultValue

    fun setValue(setupValue: SetupValue,value:String) {
        setupvalues[setupValue] = value
    }


}