package no.java.moresleep.talk

import no.java.moresleep.*

class ReadOneTalk : Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): TalkDetail {
        val talkid = parameters["id"]
        val talkInDb:TalkInDb = talkid?.let { TalkRepo.aTalk(it) }?:throw BadRequest("Unknown talkid $talkid")
        val speakersInDb:List<SpeakerInDb> = SpeakerRepo.speakersOnTalk(talkid)
        val updates = TalkRepo.updatesOnTalk(talkid)

        val talkDetail = TalkDetail(talkInDb,speakersInDb,updates)
        return talkDetail

    }

    override val requiredAccess: UserType = UserType.READ_ONLY

}