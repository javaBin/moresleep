package no.java.moresleep.talk

import no.java.moresleep.*
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.JsonGenerator
import javax.servlet.http.HttpServletResponse

class UpdateSession(val data: Map<String,DataValue>?=null,val speakers:List<SpeakerUpdate>?=null,val lastUpdated:String?=null):Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): TalkDetail {
        val id = parameters["id"]?:throw BadRequest("Missing id")
        val talkInDb:TalkInDb = TalkRepo.aTalk(id)?:throw BadRequest("Unknown talk $id")

        if (lastUpdated != null && lastUpdated != talkInDb.lastUpdated.toString()) {
            throw RequestError(HttpServletResponse.SC_CONFLICT,"Expected lastUpdated ${talkInDb.lastUpdated}, was $lastUpdated")
        }

        updateDataObject(data, talkInDb.data)
        TalkRepo.updateTalk(id,talkInDb.data,talkInDb.status)
        if (speakers != null) {
            val exsistingSpeakers = SpeakerRepo.speakersOnTalk(id)
            val createdSpeakers:MutableList<Speaker> = mutableListOf()
            for (speaker:SpeakerUpdate in speakers) {
                if (speaker.id != null && speaker.id.trim().isNotEmpty()) {
                    val exsisting:SpeakerInDb = exsistingSpeakers.firstOrNull { it.id == speaker.id}?:throw BadRequest("Unknown speaker ${speaker.id}")
                    val newName = speaker.name?:exsisting.name
                    val newEmail = speaker.email?:exsisting.email
                    val newData:JsonObject = exsisting.data
                    updateDataObject(speaker.data,newData)
                    SpeakerRepo.updateSpeaker(speaker.id,newName,newEmail,newData)
                } else {
                    createdSpeakers.add(speaker.addToDb(talkInDb.id,talkInDb.conferenceid,null))
                }
            }
            for (exsisting in exsistingSpeakers) {
                if (speakers.any { it.id == exsisting.id }) {
                    continue
                }
                SpeakerRepo.deleteSpeaker(exsisting.id)

            }
        }

        return ReadOneSession().execute(systemUser,parameters)
    }

    private fun updateDataObject(
        data: Map<String, DataValue>?,
        existingData: JsonObject
    ) {
        if (data?.isNotEmpty() == true) {
            for (entry in data.entries) {
                existingData.put(entry.key, JsonGenerator.generate(entry.value))
            }
        }
    }

    override val requiredAccess: UserType = UserType.FULLACCESS
}