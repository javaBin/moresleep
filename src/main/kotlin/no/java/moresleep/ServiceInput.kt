package no.java.moresleep

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
    SPACECAKE,
}

enum class UserType {
    ANONYMOUS,
    READ_ONLY,
    FULLACCESS,
    SUPERACCESS
}

class PathInfoMapped(val commandClass:KClass<out Command>,val parameters:Map<String,String>)

