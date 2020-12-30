package no.java.moresleep.talk

import no.java.moresleep.BadRequest
import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.UserType

class OkWithIdResult(val sessionId:String):ServiceResult()

class PublishTalk:Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): OkWithIdResult {
        val talkid = parameters["id"]?:throw BadRequest("Missing id")
        val talkInDb = TalkRepo.aTalk(talkid) ?: throw BadRequest("Unknown talk $talkid")
        val speakersInDb:List<SpeakerInDb> = SpeakerRepo.speakersOnTalk(talkid)
        val publicTalk = PublicTalk(talkInDb,speakersInDb)
        TalkRepo.publishTalk(talkid,publicTalk.jsonValue())
        return OkWithIdResult(talkid)
    }

    override val requiredAccess: UserType = UserType.FULLACCESS

}