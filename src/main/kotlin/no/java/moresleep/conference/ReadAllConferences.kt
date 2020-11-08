package no.java.moresleep.conference

import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.UserType

class Conference(val id:String,val name:String,val slug:String)

class ReadAllConferencesResult(val conferences:List<Conference>):ServiceResult()

class ReadAllConferences: Command {
    override fun execute(userType: UserType, pathInfo: String): ReadAllConferencesResult {
        val allConferences = ConferenceRepo.allConferences()
        return ReadAllConferencesResult(allConferences)
    }

}