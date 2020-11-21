package no.java.moresleep.conference

import no.java.moresleep.ServiceExecutor
import no.java.moresleep.requiredString
import no.java.moresleep.withResultSet
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

    fun allConferences():List<Conference> =
        ServiceExecutor.connection().allFromQuery("select * from conference") {
            Conference(it)
        }

    fun oneConference(conferenceid:String):Conference? =
        ServiceExecutor.connection().preparedStatement("select * from conference where id = ?") {ps ->
            ps.setString(1,conferenceid)
            ps.withResultSet {
                if (it.next()) Conference(it) else null
            }
        }


}