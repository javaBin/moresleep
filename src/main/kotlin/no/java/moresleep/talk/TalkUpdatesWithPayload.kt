package no.java.moresleep.talk

import no.java.moresleep.*

class TalkUpdatesWithPayload:Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): TalkUpdateSummary {
        val talkid = parameters["id"]?:throw BadRequest("Missing id")
        val updateList = TalkRepo.updatesWithPayloadOnTalk(talkid)
        return TalkUpdateSummary(updateList)
    }

    override val requiredAccess: UserType = UserType.READ_ONLY

}