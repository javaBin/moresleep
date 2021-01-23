package no.java.moresleep.talk

import no.java.moresleep.AllowAllOrigins
import no.java.moresleep.BadRequest
import no.java.moresleep.Command
import no.java.moresleep.UserType


class ReadAllPublicTalks:Command, AllowAllOrigins {
    override fun execute(userType: UserType, parameters: Map<String, String>): AllPublicTalks {
        val ifUnmodifiedSince = parameters["If-Unmodified-Since"]
        return parameters["id"]?.let { PublicTalkReadService.readAllPublicTalksById(it,ifUnmodifiedSince) }?:
            parameters["slug"]?.let { PublicTalkReadService.readAllPublicTalksBySlug(it,ifUnmodifiedSince)}?:
            throw BadRequest("Missing input parameter")
    }

    override val requiredAccess: UserType = UserType.ANONYMOUS

}