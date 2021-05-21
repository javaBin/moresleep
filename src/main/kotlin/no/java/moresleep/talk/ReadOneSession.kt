package no.java.moresleep.talk

import no.java.moresleep.*

class ReadOneSession : Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): TalkDetail {
        val id = parameters["id"]?:throw BadRequest("Missing id")
        val talkinDb:TalkInDb = TalkRepo.aTalk(id)?:throw BadRequest("Unknown talk $id")
        val speakerInDb:List<SpeakerInDb> = SpeakerRepo.speakersOnTalk(id)

        val updatesOnTalk = TalkRepo.updatesOnTalk(talkinDb.id)

        return TalkDetail(talkinDb,speakerInDb,updatesOnTalk)
    }

    override val requiredAccess: UserType = UserType.READ_ONLY
}