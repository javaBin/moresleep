package no.java.moresleep

import no.java.moresleep.conference.CreateNewConference
import org.jsonbuddy.JsonObject
import kotlin.reflect.KClass

enum class UserType {
    ANONYMOUS,READ_ONLY_FULLACCESS
}

enum class HttpMethod {
    GET,DELETE,POST,PUT;

    fun commandFromPathInfo(pathinfo: String):KClass<out Command>? {
        when {
            pathinfo == "/conference" -> return CreateNewConference::class
        }
        return null
    }
}

