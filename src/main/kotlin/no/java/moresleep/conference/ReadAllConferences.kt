package no.java.moresleep.conference

import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.UserType
import no.java.moresleep.requiredString
import java.sql.ResultSet

class Conference(val id:String,val name:String,val slug:String) {
    constructor(rs:ResultSet):this(
            id = rs.requiredString("id"),
            name = rs.requiredString("name"),
            slug = rs.requiredString("slug")
    )
}

class ReadAllConferencesResult(val conferences:List<Conference>):ServiceResult()

class ReadAllConferences: Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): ReadAllConferencesResult {
        val allConferences = ConferenceRepo.allConferences()
        return ReadAllConferencesResult(allConferences)
    }

    override val requiredAccess: UserType = UserType.READ_ONLY
}