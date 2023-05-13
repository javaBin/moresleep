package no.java.moresleep.conference

import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.SystemUser
import no.java.moresleep.UserType

class ReadConfig(
    val conferenceUrl:String="https://sleepingpill.javazone.no/public/allSessions/javazone_2023",
    val conferenceName:String="JavaZone 2023",
    val conferenceDates:List<String> = listOf("06.09.2023","07.09.2023"),
    val workshopDate:String="05.09.2023"
):ServiceResult()

class ReadConfigCommand:Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): ServiceResult {
        return ReadConfig()
    }

    override val requiredAccess: UserType = UserType.ANONYMOUS

}