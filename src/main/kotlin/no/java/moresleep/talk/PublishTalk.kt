package no.java.moresleep.talk

import no.java.moresleep.*

class OkWithIdResult(val sessionId:String):ServiceResult()

object PublishTalk {

    fun doPublish(talkid:String,sessionStatus: SessionStatus) {

        val talkInDb = TalkRepo.aTalk(talkid) ?: throw BadRequest("Unknown talk $talkid")
        val speakersInDb:List<SpeakerInDb> = SpeakerRepo.speakersOnTalk(talkid)
        val publicTalk = PublicTalk(talkInDb,speakersInDb)
        TalkRepo.publishTalk(talkid,publicTalk.jsonValue(),sessionStatus)
    }


}