package no.java.moresleep.talk

import no.java.moresleep.BadRequest
import no.java.moresleep.ServiceResult
import no.java.moresleep.conference.Conference
import no.java.moresleep.conference.ConferenceRepo
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class AllPublicTalks(allTalks: JsonArray): ServiceResult() {
    val readAt:LocalDateTime = LocalDateTime.now()

    private val cachedResult = JsonObject().put("sessions",allTalks)

    override fun asJsonObject(): JsonObject = cachedResult
}


object PublicTalkReadService {
    private val cachedResults:ConcurrentMap<String,AllPublicTalks> = ConcurrentHashMap()

    private val allConferences:List<Conference> by lazy { ConferenceRepo.allConferences() }

    fun readAllPublicTalksById(conferenceId:String):AllPublicTalks {
        val conference = allConferences.firstOrNull { it.id == conferenceId }?:throw BadRequest("Unknown conferenceid $conferenceId")
        return allTalks(conference)
    }

    fun readAllPublicTalksBySlug(slug:String):AllPublicTalks {
        val conference = allConferences.firstOrNull { it.slug == slug }?:throw BadRequest("Unknown slug $slug")
        return allTalks(conference)
    }

    fun allTalks(conference: Conference):AllPublicTalks {
        cachedResults[conference.id]?.let {
            if (it.readAt.plusMinutes(15).isAfter(LocalDateTime.now())) return it
        }
        val allTalks = TalkRepo.publicTalksFromConference(conference.id)
        val result = AllPublicTalks(JsonArray.fromNodeList(allTalks))
        cachedResults[conference.id] = result
        return result
    }
}