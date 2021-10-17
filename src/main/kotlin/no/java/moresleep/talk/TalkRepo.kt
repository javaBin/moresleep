package no.java.moresleep.talk

import no.java.moresleep.*
import org.jsonbuddy.JsonObject
import org.jsonbuddy.parse.JsonParser
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

fun fromDataObject(data: JsonObject):Map<String,DataValue> {
    val res:MutableMap<String,DataValue> = mutableMapOf()
    for (key in data.keys()) {
        val valueObject:JsonObject = data.requiredObject(key)
        val value = DataValue(valueObject)
        res[key] = value
    }
    return res
}

class TalkInDb(
    val id:String,
    val conferenceid: String,
    val status: SessionStatus,
    val postedBy: String?,
    val data: JsonObject,
    val lastUpdated:LocalDateTime,
    val publicdata:JsonObject?,
    val publishedAt:LocalDateTime?,
    val created:LocalDateTime,
    ) {
    constructor(rs:ResultSet):this(
            id = rs.requiredString("id"),
            conferenceid = rs.requiredString("conferenceid"),
            status = SessionStatus.valueOf(rs.requiredString("status")),
            data = JsonObject.parse(rs.requiredString("data")),
            postedBy = rs.getString("postedby"),
            lastUpdated = rs.requiredLocalDateTime("lastupdated"),
            publicdata = rs.getString("publicdata")?.let { JsonObject.parse(it) },
            publishedAt = rs.getLocalDateTime("publishedAt"),
            created = rs.requiredLocalDateTime("created"),
    )

    val dataMap:Map<String,DataValue> = fromDataObject(data)
}

class PublicTalkInDb(val content:JsonObject,val lastModified:LocalDateTime)

class TalkUpdates(
    val talkid: String,
    val updatedBy: String,
    val updatedAt:String
) {
    constructor(resultSet: ResultSet):this(
        updatedBy = SystemId.valueOf(resultSet.requiredString("updatedby")).name,
        updatedAt = resultSet.requiredLocalDateTime("updatedat").toString(),
        talkid = resultSet.requiredString("talkid")
        )
}


object TalkRepo {
    fun addNewTalk(talkid:String,conferenceid:String,status: SessionStatus,postedBy:String?,data:JsonObject,lastUpdated: LocalDateTime, publicdata:JsonObject?,publishedAt:LocalDateTime?) {
        ServiceExecutor.connection().preparedStatement("insert into talk(id,conferenceid,data,status,lastupdated,postedby,publicdata,publishedat,created) values (?,?,?,?,?,?,?,?,?)") {
            it.setString(1,talkid)
            it.setString(2,conferenceid)
            it.setString(3,data.toJson())
            it.setString(4,status.name)
            it.setTimestamp(5,lastUpdated)
            it.setString(6,postedBy)
            it.setString(7,publicdata?.toJson())
            it.setTimestamp(8,publishedAt)
            it.setTimestamp(9, LocalDateTime.now())
            it.executeUpdate()
        }
    }

    fun aTalk(talkid:String):TalkInDb? = ServiceExecutor.connection().preparedStatement("select * from talk where id = ?") {statement ->
        statement.setString(1,talkid)
        statement.withResultSet { if (it.next()) TalkInDb(it) else null }
    }

    fun allTalksInForConference(conferenceid: String):List<TalkInDb> = ServiceExecutor.connection().preparedStatement("select * from talk where conferenceid = ?") { statement ->
        statement.setString(1,conferenceid)
        statement.allFromQuery { TalkInDb(it) }
    }

    fun allTalksForEmailAddress(email:String):List<TalkInDb> = ServiceExecutor.connection().preparedStatement(
        """select distinct t.* from talk t, speaker s where s.talkid = t.id and (s.email = ? or t.postedby = ?)"""
    ) { statement ->
        statement.setString(1,email)
        statement.setString(2,email)
        statement.allFromQuery {
            TalkInDb(it)
        }
    }

    fun updateTalk(talkid: String,data: JsonObject,status: SessionStatus) {
        ServiceExecutor.connection().preparedStatement("update talk set data = ?, status = ?, lastupdated = ? where id = ?") {
            it.setString(1,data.toJson())
            it.setString(2,status.name)
            it.setTimestamp(3,LocalDateTime.now())
            it.setString(4,talkid)
            it.executeUpdate()
        }
    }

    fun publishTalk(talkid: String,publicData:JsonObject,sessionStatus: SessionStatus) {
        val now = LocalDateTime.now()
        ServiceExecutor.connection().preparedStatement("update talk set publicdata = ?, lastupdated = ?, publishedat = ?,status = ? where id = ?") {
            it.setString(1,publicData.toJson())
            it.setTimestamp(2,now)
            it.setTimestamp(3,now)
            it.setString(4,sessionStatus.name)
            it.setString(5,talkid)
            it.executeUpdate()
        }
    }

    fun unpublishTalk(talkid: String,sessionStatus: SessionStatus) {
        val now = LocalDateTime.now()
        ServiceExecutor.connection().preparedStatement("update talk set publicdata = null, lastupdated = ?, publishedat = null,status = ? where id = ?") {
            it.setTimestamp(1,now)
            it.setString(2,sessionStatus.name)
            it.setString(3,talkid)
            it.executeUpdate()
        }
    }

    fun publicTalksFromConference(conferenceid: String):List<PublicTalkInDb> = ServiceExecutor.connection().preparedStatement(
        "select publicdata,publishedat from talk where conferenceid = ? and publishedat is not null") { statement ->
        statement.setString(1,conferenceid)
        statement.allFromQuery {
            PublicTalkInDb(JsonObject.parse(it.requiredString("publicdata")),it.requiredLocalDateTime("publishedAt"))
        }
    }

    fun registerTalkUpdate(talkid: String,conferenceid: String,systemId: SystemId) {
        ServiceExecutor.connection().preparedStatement("insert into talkupdate(talkid,conferenceid, updatedby, updatedat) values (?,?,?,?)") { statement ->
            statement.setString(1,talkid)
            statement.setString(2,conferenceid)
            statement.setString(3,systemId.name)
            statement.setTimestamp(4,LocalDateTime.now())
            statement.executeUpdate()
        }
    }

    fun updatesOnTalk(talkid:String):List<TalkUpdates> = ServiceExecutor.connection().preparedStatement("select * from talkupdate where talkid = ? order by updatedat") { statement ->
        statement.setString(1,talkid)
        statement.allFromQuery { TalkUpdates(it) }
    }

    fun talkUpdatesOnConference(conferenceid: String) = ServiceExecutor.connection().preparedStatement("select * from talkupdate where conferenceid = ? order by updatedat") { statement ->
        statement.setString(1,conferenceid)
        statement.allFromQuery { TalkUpdates(it) }
    }

}
