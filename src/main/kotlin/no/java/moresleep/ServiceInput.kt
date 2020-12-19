package no.java.moresleep

import no.java.moresleep.conference.CreateNewConference
import no.java.moresleep.conference.ReadAllConferences
import no.java.moresleep.talk.CreateNewSession
import no.java.moresleep.talk.ReadAllTalks
import no.java.moresleep.talk.ReadOneTalk
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
                        Pair("/conference",CreateNewConference::class),
                        Pair("/conference/:conferenceId/session",CreateNewSession::class),
                )

            GET ->
                listOf(
                        Pair("/conference",ReadAllConferences::class),
                        Pair("/session/:id",ReadOneTalk::class),
                        Pair("/conference/:conferenceId/session",ReadAllTalks::class)
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
        var pathpos = 0
        var patternpos = 0
        val paramap:MutableMap<String,String> = mutableMapOf()
        while (patternpos < pattern.length) {
            if (pattern[patternpos] == ':') {
                patternpos++
                val patternStartPos = patternpos
                while (patternpos < pattern.length && pattern[patternpos] != '/') {
                    patternpos++
                }
                val parameterName = pattern.substring(patternStartPos,patternpos)
                val pathStartPos = pathpos
                while (pathpos < pathinfo.length && pathinfo[pathpos] != '/') {
                    pathpos++
                }
                val parameterValue = pathinfo.substring(pathStartPos,pathpos)
                paramap[parameterName] = parameterValue
            } else if (pattern[patternpos] != pathinfo[pathpos]) {
                return null
            } else {
                pathpos++
                patternpos++
            }
        }
        if (pathpos < pathinfo.length) {
            return null
        }
        return paramap
    }
}

