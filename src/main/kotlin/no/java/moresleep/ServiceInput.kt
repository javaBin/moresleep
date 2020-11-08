package no.java.moresleep

import no.java.moresleep.conference.CreateNewConference
import no.java.moresleep.conference.ReadAllConferences
import org.jsonbuddy.JsonObject
import kotlin.reflect.KClass

enum class UserType {
    ANONYMOUS,READ_ONLY,FULLACCESS
}

enum class HttpMethod {
    GET,DELETE,POST,PUT;

    fun commandFromPathInfo(pathinfo: String):KClass<out Command>? {
        when (this) {
            POST -> when {
                pathinfo == "/conference" -> return CreateNewConference::class
            }
            GET -> when {
                pathinfo == "/conference" -> return ReadAllConferences::class
            }
        }

        return null
    }
}

