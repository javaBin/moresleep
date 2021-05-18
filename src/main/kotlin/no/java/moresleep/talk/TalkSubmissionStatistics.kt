package no.java.moresleep.talk

import no.java.moresleep.*
import java.time.LocalDate

class StatisticDay(val day:String,val total:Int)

class TalkSubmissionReport(val days:List<StatisticDay>):ServiceResult()

class TalkSubmissionStatistics:Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): TalkSubmissionReport {
        val conferenceid:String = parameters["conferenceId"]?:throw BadRequest("Missing conferenceId")
        val talksForConference: List<TalkInDb> = TalkRepo.allTalksInForConference(conferenceid).sortedBy { it.created }

        val days:MutableList<StatisticDay> = mutableListOf()

        var currentStat:Pair<LocalDate,Int>? = null

        for (talk in talksForConference) {
            if (currentStat != null && currentStat.first != talk.created.toLocalDate()) {
                days.add(StatisticDay(currentStat.first.toString(),currentStat.second))
                currentStat = null
            }
            currentStat = if (currentStat == null) Pair(talk.created.toLocalDate(),1) else Pair(currentStat.first,currentStat.second+1)
        }
        if (currentStat != null) {
            days.add(StatisticDay(currentStat.first.toString(),currentStat.second))
        }

        return TalkSubmissionReport(days)
    }

    override val requiredAccess: UserType = UserType.READ_ONLY
}
