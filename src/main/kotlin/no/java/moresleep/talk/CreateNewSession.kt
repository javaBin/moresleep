package no.java.moresleep.talk

import no.java.moresleep.*
import no.java.moresleep.conference.ConferenceRepo
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.JsonGenerator


private fun toDataObject(data: Map<String,DataValue>?):JsonObject {
    if (data?.isEmpty() != false) {
        return JsonObject()
    }
    val dataObject = JsonObject()
    for (entry in data.entries) {
        dataObject.put(entry.key,JsonGenerator.generate(entry.value))
    }
    return dataObject

}


class CreateNewSession(val data: Map<String,DataValue>?=null,val postedBy:String?=null,val status:String?=null,val speakers:List<SpeakerUpdate>?=null) : Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): TalkDetail {
        val conferenceId = parameters["conferenceId"]?:throw MoresleepInternalError("Missing parameter id")
        val conf = ConferenceRepo.oneConference(conferenceId)?:throw BadRequest("Unknown conference $conferenceId")
        val sessionStatus = if (status != null) SessionStatus.saveValue(status)?:throw BadRequest("Unknown status $status") else SessionStatus.DRAFT

        if (speakers?.isNotEmpty() != true) {
            throw BadRequest("Missing speakers")
        }


        val dataObject = toDataObject(data)

        val sessionId = TalkRepo.addNewTalk(
                conferenceid = conf.id,
                status = sessionStatus,
                postedBy = postedBy,
                data = dataObject
            )

        val createdSpeakers:MutableList<Speaker> = mutableListOf()


        for (speaker in speakers) {
            if (speaker.name.isNullOrEmpty()) {
                throw BadRequest("Missing name in speaker")
            }
            if (speaker.email.isNullOrEmpty()) {
                throw BadRequest("Missing email in speaker")
            }
            val speakerid = SpeakerRepo.addSpeaker(sessionId,speaker.name,speaker.email, toDataObject(speaker.data))
            createdSpeakers.add(
                Speaker(
                    id = speakerid,
                    name = speaker.name,
                    email = speaker.email,
                    data = speaker.data?: emptyMap()
                )
            )
        }

        val talkDetail = TalkDetail(
                id = sessionId,
                postedBy = postedBy,
                data = data?: emptyMap(),
                speakers = createdSpeakers
        )
        return talkDetail
    }
}