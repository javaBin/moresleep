package no.java.moresleep.talk

import no.java.moresleep.BadRequest
import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.UserType

class AllTalks(val sessions:List<TalkDetail>):ServiceResult()

class ReadAllTalks:Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): AllTalks {
        val conferenceid:String = parameters["conferenceId"]?:throw BadRequest("Missing conferenceid")
        val allTalksInDb:List<TalkInDb> = TalkRepo.allTalksInForConference(conferenceid)
        val allSpeakersInDb:List<SpeakerInDb> = SpeakerRepo.allSpeakersInConference(conferenceid)

        val sessions:List<TalkDetail> = allTalksInDb.map { talkInDb ->
            val speakers = allSpeakersInDb.filter { it.talkId == talkInDb.id }
            TalkDetail(talkInDb,speakers)
        }
        val sortedSessions = sessions.sortedWith(TalkDetail.myComperator)
        return AllTalks(sortedSessions)
    }

    override val requiredAccess: UserType = UserType.READ_ONLY
}