package no.java.moresleep.talk

import no.java.moresleep.ServiceResult
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import java.time.LocalDateTime

private fun stripAuthorTags(original:Map<String,DataValue>,stripAuthorTags: Boolean):Map<String,DataValue> {
    if (!stripAuthorTags) {
        return original;
    }
    val newVer = original.toMutableMap()

    val removed = newVer.remove("tagswithauthor")

    val tagsWithAuthor = removed?.value as? JsonArray

    if (tagsWithAuthor?.isEmpty != false) {
        return original
    }


    val tagsarray:JsonArray = newVer.remove("tags")?.value as? JsonArray?:JsonArray()
    for (tagobj:JsonObject in tagsWithAuthor.objects { it }) {
        val newTag = (tagobj.stringValue("tag").orElse(null))?:continue
        if (tagsarray.strings().contains(newTag)) {
            continue
        }
        tagsarray.add(newTag)
    }
    newVer.put("tags",DataValue(privateData = true,value = tagsarray))
    return newVer;
}

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

    constructor(talkInDb: TalkInDb,speakers:List<SpeakerInDb>,stripAuthorTags:Boolean=false):this(
        id = talkInDb.id,
        conferenceId = talkInDb.conferenceid,
        postedBy = talkInDb.postedBy,
        data = stripAuthorTags(talkInDb.dataMap,stripAuthorTags),
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