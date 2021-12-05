package no.java.moresleep.conference

import no.java.moresleep.*
import org.jsonbuddy.JsonObject
import java.sql.ResultSet

class Conference(val id:String,val name:String,val slug:String,val slottimes:String) {
    constructor(rs:ResultSet):this(
            id = rs.requiredString("id"),
            name = rs.requiredString("name"),
            slug = rs.requiredString("slug"),
            slottimes = rs.requiredString("slottimes")
    )

    fun asPublicJson():JsonObject = JsonObject().put("id",id).put("name",name).put("slug",slug)

}

class ReadAllConferencesResult(val conferences:List<Conference>):ServiceResult()

class ReadAllConferencesPublicResult(val conferences:List<JsonObject>):ServiceResult()

class ReadAllConferences: Command, AllowAllOrigins {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): ServiceResult {
        val allConferences = ConferenceRepo.allConferences()
        if (systemUser.userType >= UserType.READ_ONLY) {
            return ReadAllConferencesResult(allConferences)
        } else {
            return ReadAllConferencesPublicResult(allConferences.map { it.asPublicJson() })
        }
    }

    override val requiredAccess: UserType = UserType.ANONYMOUS
}