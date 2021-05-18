package no.java.moresleep

interface Command {
    fun execute(systemUser: SystemUser,parameters:Map<String,String>):ServiceResult
    val requiredAccess:UserType
}

interface AllowAllOrigins {}