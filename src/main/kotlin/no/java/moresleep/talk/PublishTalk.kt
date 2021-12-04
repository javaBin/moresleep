package no.java.moresleep.talk

import no.java.moresleep.*
import no.java.moresleep.conference.Conference
import no.java.moresleep.conference.ConferenceRepo
import java.lang.RuntimeException

object PublishTalk {

    fun doPublish(talkid:String,sessionStatus: SessionStatus) {

        val talkInDb = TalkRepo.aTalk(talkid) ?: throw BadRequest("Unknown talk $talkid")


        val speakersInDb:List<SpeakerInDb> = SpeakerRepo.speakersOnTalk(talkid)
        val publicTalk = PublicTalk(talkInDb,speakersInDb)
        val publicData = publicTalk.jsonValue()
        TalkRepo.publishTalk(talkid, publicData,sessionStatus)
    }


}