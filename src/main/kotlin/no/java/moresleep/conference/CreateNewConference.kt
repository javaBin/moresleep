package no.java.moresleep.conference

import no.java.moresleep.BadRequest
import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.UserType

class CreateConferenceResult(val id:String):ServiceResult()

class CreateNewConference(val name:String?=null,val slug:String?=null):Command {
    override fun execute(userType: UserType, pathInfo: String): CreateConferenceResult {
        if (name.isNullOrEmpty() || name.trim().isEmpty()) {
            throw BadRequest("Missing required value name")
        }
        if (slug.isNullOrEmpty() || slug.trim().isEmpty()) {
            throw BadRequest("Missing required value slug")
        }
        val id = ConferenceRepo.addNewConference(name,slug)
        return CreateConferenceResult(id)
    }

}