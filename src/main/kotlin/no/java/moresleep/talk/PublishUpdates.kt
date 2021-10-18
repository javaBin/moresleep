package no.java.moresleep.talk

import no.java.moresleep.*
import org.jsonbuddy.JsonObject

class PublishOk:ServiceResult() {
    override fun asJsonObject(): JsonObject = JsonObject()
}

class PublishUpdates:Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): PublishOk {
        val talkid:String = parameters["id"]?:throw BadRequest("Missing id")
        val talkInDb:TalkInDb = TalkRepo.aTalk(talkid)?:throw BadRequest("No talk with id $talkid")
        if (talkInDb.publicdata == null) {
            throw BadRequest("Talk $talkid is not pubished. Publish before change")
        }
        PublishTalk.doPublish(talkid,talkInDb.status)
        return PublishOk()
    }

    override val requiredAccess: UserType = UserType.FULLACCESS
}