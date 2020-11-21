package no.java.moresleep

import no.java.moresleep.conference.CreateNewConference
import no.java.moresleep.conference.ReadAllConferences
import org.jsonbuddy.JsonObject
import kotlin.reflect.KClass

enum class UserType {
    ANONYMOUS,READ_ONLY,FULLACCESS
}

class PathInfoMapped(val commandClass:KClass<out Command>,val parameters:Map<String,String>)

enum class HttpMethod {
    GET,DELETE,POST,PUT;

    fun commandFromPathInfo(pathinfo: String):PathInfoMapped? {
        val decitionList:List<Pair<String,KClass<out Command>>> = when (this) {
            POST -> listOf(
                        Pair("/conference",CreateNewConference::class)
                )

            GET ->
                listOf(
                        Pair("/conference",ReadAllConferences::class)
                )
            DELETE -> TODO()
            PUT -> TODO()
        }
        for (decition in decitionList) {
            val pathmatch = mapFromPath(pathinfo,decition.first)
            if (pathmatch != null) {
                return PathInfoMapped(decition.second,pathmatch)
            }
        }

        return null
    }

    private fun mapFromPath(pathinfo: String,pattern:String):Map<String,String>? {
        if (pathinfo == pattern) return emptyMap()
        return null
    }
}

