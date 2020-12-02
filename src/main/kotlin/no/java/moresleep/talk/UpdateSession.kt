package no.java.moresleep.talk

import no.java.moresleep.BadRequest
import no.java.moresleep.Command
import no.java.moresleep.UserType
import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.JsonGenerator

class UpdateSession(val data: Map<String,DataValue>?=null,val speakers:List<SpeakerUpdate>?=null):Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): TalkDetail {
        val id = parameters["id"]?:throw BadRequest("Missing id")
        val talkInDb:TalkInDb = TalkRepo.aTalk(id)?:throw BadRequest("Unknown talk $id")

        updateDataObject(data, talkInDb.data)
        TalkRepo.updateTalk(id,talkInDb.data,talkInDb.status)
        if (speakers != null) {
            val exsistingSpeakers = SpeakerRepo.speakersOnTalk(id)
            for (speaker:SpeakerUpdate in speakers) {
                if (speaker.id != null) {
                    val exsisting:SpeakerInDb = exsistingSpeakers.firstOrNull { it.id == speaker.id}?:throw BadRequest("Unknown speaker ${speaker.id}")
                    val newName = speaker.name?:exsisting.name
                    val newEmail = speaker.email?:exsisting.email
                    val newData:JsonObject = exsisting.data
                    updateDataObject(speaker.data,newData)
                    SpeakerRepo.updateSpeaker(speaker.id,newName,newEmail,newData)
                }
            }
        }

        return ReadOneSession().execute(UserType.FULLACCESS,parameters)
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
}