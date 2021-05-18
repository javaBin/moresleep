package no.java.moresleep.talk

import no.java.moresleep.*


class ReadAllPublicTalks:Command, AllowAllOrigins {
    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): AllPublicTalks {
        val ifUnmodifiedSince = parameters["If-Unmodified-Since"]
        return parameters["id"]?.let { PublicTalkReadService.readAllPublicTalksById(it,ifUnmodifiedSince) }?:
            parameters["slug"]?.let { PublicTalkReadService.readAllPublicTalksBySlug(it,ifUnmodifiedSince)}?:
            throw BadRequest("Missing input parameter")
    }

    override val requiredAccess: UserType = UserType.ANONYMOUS

}