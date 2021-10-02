package no.java.moresleep.conference

import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.SystemUser
import no.java.moresleep.UserType

class ReadConfig(
    val conferenceUrl:String="https://sleepingpill.javazone.no/public/allSessions/javazone_2021",
    val conferenceName:String="JavaZone 2021",
    val conferenceDates:List<String> = listOf("08.12.2021","09.12.2021"),
    val workshopDate:String="07.12.2021"
):ServiceResult()

class ReadConfigCommand:Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): ServiceResult {
        return ReadConfig()
    }

    override val requiredAccess: UserType = UserType.ANONYMOUS

}