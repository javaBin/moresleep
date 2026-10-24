package no.java.moresleep.conference

import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.SystemUser
import no.java.moresleep.UserType

class ReadConfig(
    val conferenceUrl:String="https://sleepingpill.javazone.no/public/allSessions/javazone_2026",
    val conferenceName:String="JavaZone 2026",
    val conferenceDates:List<String> = listOf("02.09.2026","03.09.2026"),
    val workshopDate:String="01.09.2026"
):ServiceResult()

class ReadConfigCommand:Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): ServiceResult {
        return ReadConfig()
    }

    override val requiredAccess: UserType = UserType.ANONYMOUS

}