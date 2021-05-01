package no.java.moresleep.talk

import no.java.moresleep.BadRequest
import no.java.moresleep.Command
import no.java.moresleep.UserType

class ReadTalksBySubmitter:Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): AllTalks {
        val email:String = parameters["email"]?:throw BadRequest("Missing email parameter")
        val talksDbBySpeaker = TalkRepo.allTalksForEmailAddress(email)
        val talksBySpeaker:List<TalkDetail> = talksDbBySpeaker.map {
            val speakers = SpeakerRepo.speakersOnTalk(it.id)
            TalkDetail(it,speakers,true)
        }
        return AllTalks(talksBySpeaker)
    }

    override val requiredAccess: UserType = UserType.READ_ONLY

}