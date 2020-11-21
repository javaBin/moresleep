package no.java.moresleep.talk

import no.java.moresleep.ServiceResult

class TalkDetail(val id:String,val postedBy:String?,val data: Map<String,DataValue>) : ServiceResult() {


    val sessionId = id

    constructor(talkInDb: TalkInDb):this(talkInDb.id,talkInDb.postedBy,talkInDb.dataMap)
}