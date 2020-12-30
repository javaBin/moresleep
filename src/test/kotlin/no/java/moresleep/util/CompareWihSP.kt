package no.java.moresleep.util

import no.java.moresleep.*
import no.java.moresleep.talk.AllTalks
import no.java.moresleep.talk.ReadAllTalks
import org.jsonbuddy.JsonObject
import java.io.File

fun main(args: Array<String>) {
    if (args.size < 2) {
        println ("Usage configfile")
        return
    }
    Setup.loadFromFile(args)
    Database.migrateWithFlyway(null,null)
    val conferenceId = args[1]
    val allTalks:AllTalks = ServiceExecutor.createConnection().use {
        ReadAllTalks().execute(UserType.READ_ONLY, mapOf(Pair("conferenceId", conferenceId)))
    }
    val conn = PopulateWorker.openConnectionToSleepingpill("${Setup.readValue(SetupValue.SLEEPING_PILL_ADDR)}/data/conference/$conferenceId/session")
    val sleepingpillobj = JsonObject.read(conn)
    val sleepingPillTalks:List<JsonObject> = sleepingpillobj.requiredArray("sessions").objects { it }
    val mytalkobj = allTalks.asJsonObject()
    val myTalks:List<JsonObject> = mytalkobj.requiredArray("sessions").objects { it }
    println("Read ${myTalks.size}")
    println("Read sp ${sleepingPillTalks.size}")
    //File("/Users/anderskarlsen/Downloads/sptalks.json").writeText(sleepingpillobj.toJson(),Charsets.UTF_8)
    //File("/Users/anderskarlsen/Downloads/mytalks.json").writeText(mytalkobj.toJson(),Charsets.UTF_8)
    CompareWihSP(sleepingPillTalks,myTalks).runCompare()
}

class CompareWihSP(val sleepingPillTalks:List<JsonObject>,val myTalks:List<JsonObject>) {

    val allMissingKeys:MutableSet<String> = mutableSetOf()

    fun runCompare() {
        for (sptalk in sleepingPillTalks) {
            compareReport(sptalk,myTalks)
        }
        println ("Missing keys $allMissingKeys")
    }

    private fun compareReport(sptalk:JsonObject,myTalks:List<JsonObject>) {
        val talkid = sptalk.requiredString("id")
        val myTalk:JsonObject? = myTalks.firstOrNull { it.requiredString("id") == talkid }
        if (myTalk == null) {
            println ("Did not find talk $talkid")
            return
        }
        val spkeyes = sptalk.keys()
        val mykeys = myTalk.keys()
        val missingKeys = spkeyes.filter { !mykeys.contains(it) }
        allMissingKeys.addAll(missingKeys)
    }

}

