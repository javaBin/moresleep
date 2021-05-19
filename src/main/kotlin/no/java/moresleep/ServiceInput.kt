package no.java.moresleep

import no.java.moresleep.conference.CreateNewConference
import no.java.moresleep.conference.ReadAllConferences
import no.java.moresleep.conference.ReadConfigCommand
import no.java.moresleep.talk.*
import kotlin.reflect.KClass

class SystemUser(val userType:UserType, val systemId: SystemId,val basicAuthAccessDev:String?=null)

enum class SystemId {
    UNKNOWN,
    SUBMITIT,
    CAKE,
    MORESLEEP_WORKER,
    MORESLEEP_ADMIN,
    ANONYMOUS,
    READ_ONLY_SYSTEM,
}

enum class UserType {
    ANONYMOUS,
    READ_ONLY,
    FULLACCESS,
    SUPERACCESS
}

class PathInfoMapped(val commandClass:KClass<out Command>,val parameters:Map<String,String>)

enum class HttpMethod {
    GET,DELETE,POST,PUT;

    fun commandFromPathInfo(pathinfo: String,additionalParas: Map<String, String>):PathInfoMapped? {
        val decitionList:List<Pair<String,KClass<out Command>>> = when (this) {
            POST -> listOf(
                        Pair("/data/conference",CreateNewConference::class),
                        Pair("/data/conference/:conferenceId/session",CreateNewSession::class),

                )

            GET ->
                listOf(
                        Pair("/data/conference",ReadAllConferences::class),
                        Pair("/public/allSessions",ReadAllConferences::class),
                        Pair("/data/session/:id",ReadOneTalk::class),
                        Pair("/data/conference/:conferenceId/session",ReadAllTalks::class),
                        Pair("/data/conference/:conferenceId/substatistics", TalkSubmissionStatistics::class),

                        Pair("/data/submitter/:email/session",ReadTalksBySubmitter::class),
                        Pair("/public/allSessions/:slug",ReadAllPublicTalks::class),
                        Pair("/public/conference/:id/session",ReadAllPublicTalks::class),
                        Pair("/public/config",ReadConfigCommand::class),

                )
            DELETE -> emptyList()
            PUT -> listOf(
                Pair("/data/session/:id",UpdateSession::class)
            )
        }
        for (decition in decitionList) {
            val pathmatch = mapFromPath(pathinfo,decition.first,additionalParas)
            if (pathmatch != null) {
                return PathInfoMapped(decition.second,pathmatch)
            }
        }

        return null
    }

    private fun mapFromPath(pathinfo: String,pattern:String,additionalParas:Map<String,String>):Map<String,String>? {
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
        paramap.putAll(additionalParas)
        return paramap
    }
}

