package no.java.moresleep

interface Command {
    fun execute(userType: UserType,pathInfo:String):ServiceResult
}