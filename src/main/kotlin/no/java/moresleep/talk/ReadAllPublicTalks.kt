package no.java.moresleep.talk

import no.java.moresleep.BadRequest
import no.java.moresleep.Command
import no.java.moresleep.UserType


class ReadAllPublicTalks:Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): AllPublicTalks =
        parameters["id"]?.let { PublicTalkReadService.readAllPublicTalksById(it) }?:
        parameters["slug"]?.let { PublicTalkReadService.readAllPublicTalksBySlug(it)}?:
        throw BadRequest("Missing input parameter")

    override val requiredAccess: UserType = UserType.ANONYMOUS

}