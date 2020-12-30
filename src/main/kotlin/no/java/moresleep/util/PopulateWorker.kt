package no.java.moresleep.util

import no.java.moresleep.*
import no.java.moresleep.conference.Conference
import no.java.moresleep.conference.ConferenceRepo
import no.java.moresleep.conference.CreateNewConference
import no.java.moresleep.talk.CreateNewSession
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.PojoMapper
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object PopulateWorker {
    private val SLEEPING_PILL_ADDR by lazy { Setup.readValue(SetupValue.SLEEPING_PILL_ADDR) }


    fun populateAll() {
        if (!Setup.readBoolValue(SetupValue.LOAD_FROM_SLEEPINGPILL) || Setup.readValue(SetupValue.SLEEPINGPILL_AUTH).isBlank()) {
            return
        }
        val allConfsOld = ServiceExecutor.createConnection().use {
            ConferenceRepo.allConferences()
        }
        if (allConfsOld.isNotEmpty()) {
            return
        }
        ServiceExecutor.createConnection().use {
            readAllConferencesPoulate()
            ServiceExecutor.commit()
            for (conference in ConferenceRepo.allConferences()) {
                addTalksFromConference(conference)
                ServiceExecutor.commit()
            }
        }

    }

    private fun readAllConferencesPoulate() {
        val allConfs:List<JsonObject> = readAllConferencesFromSp().objects({it})
        for (conference in allConfs) {
            addConference(conference)
        }

    }

    private fun addTalksFromConference(conference: Conference) {
        val conferenceId:String = conference.id
        val conn = openConnectionToSleepingpill("$SLEEPING_PILL_ADDR/data/conference/$conferenceId/session")
        val obj:JsonObject = JsonObject.read(conn)
        val sessions:List<JsonObject> = obj.requiredArray("sessions").objects { it }
        println("Adding ${sessions.size} from ${conference.name}")
        for (spsession in sessions) {
            val createNewSession = PojoMapper.map(spsession,CreateNewSession::class.java)
            try {
                createNewSession.execute(UserType.SUPERACCESS, mapOf("conferenceId" to conferenceId))
                ServiceExecutor.commit()
            } catch (br:BadRequest) {
                ServiceExecutor.rollback()
            }

        }
    }


    private fun readAllConferencesFromSp():JsonArray {
        val conn = openConnectionToSleepingpill("$SLEEPING_PILL_ADDR/data/conference")
        val obj:JsonObject = JsonObject.read(conn)
        return obj.requiredArray("conferences")
    }

    private fun addConference(sleepingPillConference:JsonObject) {
        val createNewConference:CreateNewConference = PojoMapper.map(sleepingPillConference,CreateNewConference::class.java)
        createNewConference.execute(UserType.SUPERACCESS, emptyMap())
        ServiceExecutor.commit()
    }

    fun openConnectionToSleepingpill(urlpath:String):HttpURLConnection {
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