package no.java.moresleep.conference

import no.java.moresleep.*
import java.sql.ResultSet

class Conference(val id:String,val name:String,val slug:String) {
    constructor(rs:ResultSet):this(
            id = rs.requiredString("id"),
            name = rs.requiredString("name"),
            slug = rs.requiredString("slug")
    )


}

class ReadAllConferencesResult(val conferences:List<Conference>):ServiceResult()

class ReadAllConferences: Command, AllowAllOrigins {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): ReadAllConferencesResult {
        val allConferences = ConferenceRepo.allConferences()
        return ReadAllConferencesResult(allConferences)
    }

    override val requiredAccess: UserType = UserType.ANONYMOUS
}