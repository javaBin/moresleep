package no.java.moresleep.talk

import no.java.moresleep.BadRequest
import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.UserType
import org.jsonbuddy.pojo.JsonGenerator

class UpdateSession(val data: Map<String,DataValue>?=null):Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): TalkDetail {
        val id = parameters["id"]?:throw BadRequest("Missing id")
        val talkInDb:TalkInDb = TalkRepo.aTalk(id)?:throw BadRequest("Unknown talk $id")

        if (data?.isNotEmpty() == true) {
            for (entry in data.entries) {
                talkInDb.data.put(entry.key,JsonGenerator.generate(entry.value))
            }
        }

        TalkRepo.updateTalk(id,talkInDb.data,talkInDb.status)

        return ReadOneSession().execute(UserType.FULLACCESS,parameters)
    }
}