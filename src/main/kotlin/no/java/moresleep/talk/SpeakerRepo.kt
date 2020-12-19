package no.java.moresleep.talk

import no.java.moresleep.ServiceExecutor
import no.java.moresleep.allFromQuery
import no.java.moresleep.requiredString
import org.jsonbuddy.JsonObject
import java.sql.ResultSet
import java.util.*

class SpeakerInDb(val id:String,val talkId: String,val name:String,val email: String,val data: JsonObject) {
    constructor(rs:ResultSet):this(
        id = rs.requiredString("id"),
        talkId = rs.requiredString("talkid"),
        name = rs.requiredString("name"),
        email = rs.requiredString("email"),
        data = JsonObject.parse(rs.requiredString("data"))
    )

    val dataMap:Map<String,DataValue> = fromDataObject(data)
}

object SpeakerRepo {
    fun addSpeaker(id:String,talkId:String,conferenceid:String,name:String,email:String,data:JsonObject) {
        ServiceExecutor.connection().preparedStatement("insert into speaker(id,talkid,conferenceid,name,email,data) values (?,?,?,?,?,?)") {
            it.setString(1,id)
            it.setString(2,talkId)
            it.setString(3,conferenceid)
            it.setString(4,name)
            it.setString(5,email)
            it.setString(6,data.toJson())
            it.executeUpdate()
        }
    }

    fun speakersOnTalk(talkId:String):List<SpeakerInDb> = ServiceExecutor.connection().preparedStatement("select * from speaker where talkid = ?") {statement ->
        statement.setString(1,talkId)
        statement.allFromQuery { SpeakerInDb(it) }
    }

    fun allSpeakersInConference(conferenceid: String):List<SpeakerInDb> = ServiceExecutor.connection().preparedStatement("select * from speaker where conferenceid = ?") { statement ->
        statement.setString(1,conferenceid)
        statement.allFromQuery { SpeakerInDb(it) }
    }

    fun updateSpeaker(speakerid:String,name:String,email: String,data:JsonObject) {
        ServiceExecutor.connection().preparedStatement("update speaker set name = ?, email = ?, data= ? where id = ?") {
            it.setString(1,name)
            it.setString(2,email)
            it.setString(3,data.toJson())
            it.setString(4,speakerid)
            it.executeUpdate()

        }
    }

    fun deleteSpeaker(speakerid: String) {
        ServiceExecutor.connection().preparedStatement("delete from speaker where id = ?") {
            it.setString(1,speakerid)
            it.executeUpdate()
        }
    }

}