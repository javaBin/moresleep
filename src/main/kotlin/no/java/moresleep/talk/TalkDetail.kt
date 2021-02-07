package no.java.moresleep.talk

import no.java.moresleep.ServiceResult
import java.time.LocalDateTime

class TalkDetail(
    val id:String,
    val conferenceId:String,
    val postedBy:String?,
    val data: Map<String,DataValue>,
    val status:SessionStatus,
    val speakers:List<Speaker>,
    val lastUpdated:String,
    val sessionUpdates:SessionUpdates = SessionUpdates(null, emptyList()),
    private val created:LocalDateTime,
) : ServiceResult() {

    val sessionId = id

    constructor(talkInDb: TalkInDb,speakers:List<SpeakerInDb>):this(
        id = talkInDb.id,
        conferenceId = talkInDb.conferenceid,
        postedBy = talkInDb.postedBy,
        data = talkInDb.dataMap,
        status = talkInDb.status,
        lastUpdated = talkInDb.lastUpdated.toString(),
        sessionUpdates = SessionUpdates(talkInDb,speakers),
        speakers = speakers.map { Speaker(
            id = it.id,
            name = it.name,
            email = it.email,
            data = fromDataObject(it.data)
        ) },
        created = talkInDb.created
    )

    companion object {
        val myComperator:Comparator<TalkDetail> =
            Comparator { o1, o2 -> o1.created.compareTo(o2.created) }
    }
}