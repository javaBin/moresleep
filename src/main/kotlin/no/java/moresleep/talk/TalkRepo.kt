package no.java.moresleep.talk

import no.java.moresleep.*
import org.jsonbuddy.JsonObject
import org.jsonbuddy.parse.JsonParser
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

class TalkInDb(val id:String,val conferenceid: String,val status: SessionStatus,val postedBy: String?,val data: JsonObject,val lastUpdated:LocalDateTime) {
    constructor(rs:ResultSet):this(
            id = rs.requiredString("id"),
            conferenceid = rs.requiredString("conferenceid"),
            status = SessionStatus.valueOf(rs.requiredString("status")),
            data = JsonObject.parse(rs.requiredString("data")),
            postedBy = rs.getString("postedby"),
            lastUpdated = rs.requiredLocalDateTime("lastupdated")
    )

    val dataMap:Map<String,DataValue> = {
        val res:MutableMap<String,DataValue> = mutableMapOf()
        for (key in data.keys()) {
            val valueObject:JsonObject = data.requiredObject(key)
            val value = DataValue(valueObject)
            res[key] = value
        }
        res
    }()
}

object TalkRepo {
    fun addNewTalk(conferenceid:String,status: SessionStatus,postedBy:String?,data:JsonObject):String {
        val id = UUID.randomUUID().toString()
        ServiceExecutor.connection().preparedStatement("insert into talk(id,conferenceid,data,status,lastupdated,postedby) values (?,?,?,?,?,?)") {
            it.setString(1,id)
            it.setString(2,conferenceid)
            it.setString(3,data.toJson())
            it.setString(4,status.name)
            it.setTimestamp(5,LocalDateTime.now())
            it.setString(6,postedBy)
            it.executeUpdate()
        }
        return id
    }

    fun aTalk(talkid:String):TalkInDb? = ServiceExecutor.connection().preparedStatement("select * from talk where id = ?") {statement ->
        statement.setString(1,talkid)
        statement.withResultSet { if (it.next()) TalkInDb(it) else null }
    }

}