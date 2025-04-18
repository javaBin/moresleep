package no.java.moresleep.talk

import no.java.moresleep.*
import no.java.moresleep.conference.ConferenceRepo
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.JsonGenerator
import java.time.LocalDateTime
import java.util.*


fun toDataObject(data: Map<String,DataValue>?):JsonObject {
    if (data?.isEmpty() != false) {
        return JsonObject()
    }
    val dataObject = JsonObject()
    for (entry in data.entries) {
        dataObject.put(entry.key,JsonGenerator.generate(entry.value))
    }
    return dataObject

}


class CreateNewSession(val data: Map<String,DataValue>?=null,val postedBy:String?=null,val status:String?=null,val speakers:List<SpeakerUpdate>?=null,val id:String?=null,val lastUpdated:String?=null) : Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): TalkDetail {
        val conferenceId = parameters["conferenceId"]?:throw MoresleepInternalError("Missing parameter id")
        val conf = ConferenceRepo.oneConference(conferenceId)?:throw BadRequest("Unknown conference $conferenceId")
        val sessionStatus = if (status != null) SessionStatus.saveValue(status)?:throw BadRequest("Unknown status $status") else SessionStatus.DRAFT
        if ((id != null || lastUpdated != null) && systemUser.userType != UserType.SUPERACCESS) {
            throw ForbiddenRequest("No id or lastUpdated allowed")
        }

        if (speakers?.isNotEmpty() != true) {
            throw BadRequest("Missing speakers")
        }


        val dataObject:JsonObject = toDataObject(data)

        val sessionId:String = id?:UUID.randomUUID().toString()

        val lastUpdatedTime = lastUpdated?.let { LocalDateTime.parse(it) }?:LocalDateTime.now()
        TalkRepo.addNewTalk(
                talkid = sessionId,
                conferenceid = conf.id,
                status = sessionStatus,
                postedBy = postedBy,
                data = dataObject,
                lastUpdated = lastUpdatedTime,
                publicdata = null,
                publishedAt = null
            )

        val createdSpeakers:MutableList<Speaker> = mutableListOf()


        for (speaker:SpeakerUpdate in speakers) {
            if (!speaker.id.isNullOrEmpty() && systemUser.userType != UserType.SUPERACCESS) {
                throw ForbiddenRequest("No id allowed on speaker")
            }
            createdSpeakers.add(speaker.addToDb(sessionId,conferenceId,systemUser,if (speaker.id.isNullOrEmpty()) null else speaker.id))
        }

        if (id != null && SessionStatus.publcStatuses.contains(sessionStatus)) {
            PublishTalk.doPublish(id,sessionStatus)
        }

        TalkRepo.registerTalkUpdate(
            talkid = sessionId,
            conferenceid = conf.id,
            systemId = systemUser.systemId,
            payload = if (Setup.readBoolValue(SetupValue.STORE_UPDATES)) dataObject else null
        )

        return ReadOneTalk().execute(systemUser, mapOf(Pair("id",sessionId)))


    }

    override val requiredAccess: UserType = UserType.FULLACCESS
}