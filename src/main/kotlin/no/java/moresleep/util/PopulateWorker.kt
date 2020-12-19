package no.java.moresleep.util

import no.java.moresleep.*
import no.java.moresleep.conference.CreateNewConference
import no.java.moresleep.talk.CreateNewSession
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.PojoMapper
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class PopulateWorker {
    private val SLEEPING_PILL_ADDR = "https://sleepingpill.javazone.no"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val me = PopulateWorker()
            Setup.loadFromFile(args)



            Database.migrateWithFlyway(null,null)
            ServiceExecutor.createConnection().use {
                me.addTalksFromConference("02a3a811-afe1-48d3-a64b-10d9732b3735")
            }
        }
    }

    fun readAllConferencesPoulate() {
        val allConfs:List<JsonObject> = readAllConferencesFromSp().objects({it})
        for (conference in allConfs) {
            addConference(conference)
        }
    }

    fun addTalksFromConference(conferenceId:String) {
        val conn = openConnection("$SLEEPING_PILL_ADDR/data/conference/$conferenceId/session")
        val obj:JsonObject = JsonObject.read(conn)
        val sessions:List<JsonObject> = obj.requiredArray("sessions").objects { it }
        println ("Starting ${sessions.size}")
        var num=0
        for (spsession in sessions) {
            val createNewSession = PojoMapper.map(spsession,CreateNewSession::class.java)
            createNewSession.execute(UserType.SUPERACCESS, mapOf("conferenceId" to conferenceId))
            ServiceExecutor.commit()
            num++
            println("Done $num")
        }
    }


    private fun readAllConferencesFromSp():JsonArray {
        val conn = openConnection("$SLEEPING_PILL_ADDR/data/conference")
        val obj:JsonObject = JsonObject.read(conn)
        return obj.requiredArray("conferences")
    }

    private fun addConference(sleepingPillConference:JsonObject) {
        val createNewConference:CreateNewConference = PojoMapper.map(sleepingPillConference,CreateNewConference::class.java)
        createNewConference.execute(UserType.SUPERACCESS, emptyMap())
        ServiceExecutor.commit()
    }

    private fun openConnection(urlpath:String):HttpURLConnection {
        val authString = Setup.readValue(SetupValue.SLEEPINGPILL_AUTH)
        if (authString.isBlank()) {
            throw RuntimeException("Needs to set sleepingpill auth")
        }
        val url = URL(urlpath)
        val urlConnection = url.openConnection() as HttpURLConnection
        val authStringEnc: String = Base64.getEncoder().encodeToString(authString.toByteArray(Charsets.UTF_8))
        urlConnection.setRequestProperty("Authorization", "Basic $authStringEnc")
        return urlConnection
    }
}