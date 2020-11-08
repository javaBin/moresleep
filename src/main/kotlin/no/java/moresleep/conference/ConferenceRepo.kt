package no.java.moresleep.conference

import no.java.moresleep.ServiceExecutor
import java.util.*

object ConferenceRepo {
    fun addNewConference(name:String,slug:String):String {
        val id = UUID.randomUUID().toString()
        ServiceExecutor.connection().preparedStatement("insert into conference(id,name,slug) values (?,?,?)") {
            it.setString(1,id)
            it.setString(2,name)
            it.setString(3,slug)
            it.executeUpdate()
        }
        return id
    }
}