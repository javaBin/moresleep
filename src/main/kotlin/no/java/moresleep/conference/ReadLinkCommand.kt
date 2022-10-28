package no.java.moresleep.conference

import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.SystemUser
import no.java.moresleep.UserType
import org.jsonbuddy.JsonObject

class ResultWithLink(private val link:String):ServiceResult() {
    override fun asJsonObject(): JsonObject = JsonObject().put("link",link)

}

class ReadLinkCommand:Command {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): ResultWithLink {
        return ResultWithLink("https://www.java.no")
    }

    override val requiredAccess: UserType = UserType.ANONYMOUS

}