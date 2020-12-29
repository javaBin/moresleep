package no.java.moresleep.talk

import no.java.moresleep.BadRequest
import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.UserType

class ReadOneSession : Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): TalkDetail {
        val id = parameters["id"]?:throw BadRequest("Missing id")
        val talkinDb:TalkInDb = TalkRepo.aTalk(id)?:throw BadRequest("Unknown talk $id")
        val speakerInDb:List<SpeakerInDb> = SpeakerRepo.speakersOnTalk(id)
        return TalkDetail(talkinDb,speakerInDb)
    }

    override val requiredAccess: UserType = UserType.READ_ONLY
}