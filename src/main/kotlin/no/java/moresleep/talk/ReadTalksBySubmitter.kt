package no.java.moresleep.talk

import no.java.moresleep.Command
import no.java.moresleep.ServiceResult
import no.java.moresleep.UserType

class ReadTalksBySubmitter:Command {
    override fun execute(userType: UserType, parameters: Map<String, String>): AllTalks {
        return AllTalks(emptyList())
    }

    override val requiredAccess: UserType = UserType.READ_ONLY

}