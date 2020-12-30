package no.java.moresleep.talk

import no.java.moresleep.ServiceResult

class TalkDetail(val id:String,val postedBy:String?,val data: Map<String,DataValue>,val status:SessionStatus,val speakers:List<Speaker>,val lastUpdated:String) : ServiceResult() {

    val sessionUpdates = SessionUpdates()

    val sessionId = id

    constructor(talkInDb: TalkInDb,speakers:List<SpeakerInDb>):this(
        id = talkInDb.id,
        postedBy = talkInDb.postedBy,
        data = talkInDb.dataMap,
        status = talkInDb.status,
        lastUpdated = talkInDb.lastUpdated.toString(),
        speakers = speakers.map { Speaker(
            id = it.id,
            name = it.name,
            email = it.email,
            data = fromDataObject(it.data)
        ) }

    )
}