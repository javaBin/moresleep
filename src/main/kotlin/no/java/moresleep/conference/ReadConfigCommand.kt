package no.java.moresleep.conference

import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.SystemUser
import no.java.moresleep.UserType

class ReadConfig(
    val conferenceUrl:String="https://sleepingpill.javazone.no/public/allSessions/javazone_2024",
    val conferenceName:String="JavaZone 2024",
    val conferenceDates:List<String> = listOf("04.09.2024","05.09.2024"),
    val workshopDate:String="03.09.2024"
):ServiceResult()

class ReadConfigCommand:Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): ServiceResult {
        return ReadConfig()
    }

    override val requiredAccess: UserType = UserType.ANONYMOUS

}