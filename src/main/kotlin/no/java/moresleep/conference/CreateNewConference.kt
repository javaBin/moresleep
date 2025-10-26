package no.java.moresleep.conference

import no.java.moresleep.*
import no.java.moresleep.hooks.HookMessage
import no.java.moresleep.hooks.HookMessageType
import no.java.moresleep.hooks.HookReporter
import javax.servlet.http.HttpServletResponse

class CreateConferenceResult(val id:String):ServiceResult()

class CreateNewConference(val name:String?=null,val slug:String?=null,val id:String?=null):Command {

    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): CreateConferenceResult {
        if (name.isNullOrEmpty() || name.trim().isEmpty()) {
            throw BadRequest("Missing required value name")
        }
        if (slug.isNullOrEmpty() || slug.trim().isEmpty()) {
            throw BadRequest("Missing required value slug")
        }
        if (id != null && systemUser.userType != UserType.SUPERACCESS) {
            throw ForbiddenRequest("No id allowed")
        }
        val id = ConferenceRepo.addNewConference(name,slug,id)
        HookReporter.reportHook(HookMessage(
            type = HookMessageType.ADDED_CONFERENCE,
            reference = id
        ))
        return CreateConferenceResult(id)
    }

    override val requiredAccess: UserType = UserType.FULLACCESS

}