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
    fun addSpeaker(talkId:String,name:String,email:String,data:JsonObject):String {
        val id = UUID.randomUUID().toString()
        ServiceExecutor.connection().preparedStatement("insert into speaker(id,talkid,name,email,data) values (?,?,?,?,?)") {
            it.setString(1,id)
            it.setString(2,talkId)
            it.setString(3,name)
            it.setString(4,email)
            it.setString(5,data.toJson())
            it.executeUpdate()
        }
        return id
    }

    fun speakersOnTalk(talkId:String):List<SpeakerInDb> = ServiceExecutor.connection().preparedStatement("select * from speaker where talkid = ?") {statement ->
        statement.setString(1,talkId)
        statement.allFromQuery { SpeakerInDb(it) }
    }
}