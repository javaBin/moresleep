package no.java.moresleep.talk

import no.java.moresleep.BadRequest
import no.java.moresleep.RequestError
import no.java.moresleep.ServiceResult
import no.java.moresleep.conference.Conference
import no.java.moresleep.conference.ConferenceRepo
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.servlet.http.HttpServletResponse

class AllPublicTalks(allTalks: JsonArray,val lastModified:LocalDateTime): ServiceResult() {
    val readAt:LocalDateTime = LocalDateTime.now()

    private val cachedResult = JsonObject().put("sessions",allTalks)

    override fun asJsonObject(): JsonObject = cachedResult
}


object PublicTalkReadService {
    private val cachedResults:ConcurrentMap<String,AllPublicTalks> = ConcurrentHashMap()

    private val allConferences:List<Conference> by lazy { ConferenceRepo.allConferences() }

    fun readAllPublicTalksById(conferenceId:String,ifUnmodifiedSince:String?):AllPublicTalks {
        val conference = allConferences.firstOrNull { it.id == conferenceId }?:throw BadRequest("Unknown conferenceid $conferenceId")
        return allTalks(conference,ifUnmodifiedSince)
    }

    fun readAllPublicTalksBySlug(slug:String,ifUnmodifiedSince:String?):AllPublicTalks {
        val conference = allConferences.firstOrNull { it.slug == slug }?:throw BadRequest("Unknown slug $slug")
        return allTalks(conference,ifUnmodifiedSince)
    }

    private fun allTalks(conference: Conference,ifUnmodifiedSince:String?):AllPublicTalks {
        val readTalks = readTalks(conference)

        if (ifUnmodifiedSince == null) {
            return readTalks
        }
        val checkTime:LocalDateTime = ZonedDateTime.parse(ifUnmodifiedSince, DateTimeFormatter.RFC_1123_DATE_TIME)
            .withZoneSameInstant(ZoneId.of("Europe/Oslo"))
            .toLocalDateTime()

        if (checkTime.isAfter(readTalks.lastModified)) {
            throw RequestError(HttpServletResponse.SC_PRECONDITION_FAILED,"Not modified")
        }

        return readTalks
    }

    private fun readTalks(conference: Conference): AllPublicTalks {
        cachedResults[conference.id]?.let {
            if (it.readAt.plusMinutes(15).isAfter(LocalDateTime.now())) return it
        }
        val allTalks: List<PublicTalkInDb> = TalkRepo.publicTalksFromConference(conference.id)

        val lastModified:LocalDateTime = allTalks.maxByOrNull { it.lastModified }?.lastModified?:LocalDateTime.now()
        val result = AllPublicTalks(JsonArray.fromNodeStream(allTalks.stream().map { it.content }),lastModified)
        cachedResults[conference.id] = result
        return result
    }
}