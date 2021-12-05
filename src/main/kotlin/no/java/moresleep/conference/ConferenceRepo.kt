package no.java.moresleep.conference

import no.java.moresleep.ServiceExecutor
import no.java.moresleep.requiredString
import no.java.moresleep.withResultSet
import java.util.*

object ConferenceRepo {
    private const val DEFAULT_SLOTS = "09:00,10:20,11:40,13:00,14:20,15:40,17:00,18:20";

    fun addNewConference(name:String,slug:String,givenid:String?):String {
        val id = givenid?:UUID.randomUUID().toString()
        ServiceExecutor.connection().preparedStatement("insert into conference(id,name,slug,slottimes) values (?,?,?,?)") {
            it.setString(1,id)
            it.setString(2,name)
            it.setString(3,slug)
            it.setString(4, DEFAULT_SLOTS)
            it.executeUpdate()
        }
        return id
    }

    fun updateSlottimes(conferenceid: String,slottimes:String) {
        ServiceExecutor.connection().preparedStatement("update conference set slottimes = ? where id = ?") {
            it.setString(1,slottimes)
            it.setString(2,conferenceid)
            it.executeUpdate()
        }
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