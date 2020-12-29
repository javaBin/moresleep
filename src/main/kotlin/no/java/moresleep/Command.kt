package no.java.moresleep

interface Command {
    fun execute(userType: UserType,parameters:Map<String,String>):ServiceResult
    val requiredAccess:UserType
}