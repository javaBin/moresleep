package no.java.moresleep.talk

import no.java.moresleep.*
import no.java.moresleep.conference.ConferenceRepo
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.JsonGenerator


class CreateNewSession(val data: Map<String,DataValue>?=null,val postedBy:String?=null,val status:String?=null) : Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): TalkDetail {
        val conferenceId = parameters["conferenceId"]?:throw MoresleepInternalError("Missing parameter id")
        val conf = ConferenceRepo.oneConference(conferenceId)?:throw BadRequest("Unknown conference $conferenceId")
        val sessionStatus = SessionStatus.valueOf(status!!)

        val dataNonNull = data?: emptyMap()

        val dataObject = JsonObject()
        for (entry in dataNonNull.entries) {
            dataObject.put(entry.key,JsonGenerator.generate(entry.value))
        }

        val sessionId = TalkRepo.addNewTalk(
                conferenceid = conf.id,
                status = sessionStatus,
                postedBy = postedBy,
                data = dataObject
            )
        val talkDetail = TalkDetail(
                id = sessionId,
                postedBy = postedBy,
                data = data?: emptyMap(),

        )
        return talkDetail
    }
}