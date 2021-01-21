package no.java.moresleep.talk

import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonNode
import org.jsonbuddy.JsonObject
import org.jsonbuddy.JsonString
import org.jsonbuddy.pojo.JsonPojoBuilder
import org.jsonbuddy.pojo.OverrideMapper
import org.jsonbuddy.pojo.OverridesJsonGenerator
import org.jsonbuddy.pojo.PojoMapper
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private fun toPublicMap(dataMap:Map<String,DataValue>):Map<String,String> {
    val res:MutableMap<String,String> = mutableMapOf()
    for (entry in dataMap.entries) {
        if (entry.value.privateData || entry.value.value !is JsonString) {
            continue
        }
        val value = entry.value.value
        if (value !is JsonString) {
            continue
        }
        res[entry.key] = value.stringValue()
    }
    res["startTime"]?.let {
        res["startTimeZulu"] = toZuluTimeString(it)
    }
    res["endTime"]?.let {
        res["endTimeZulu"] = toZuluTimeString(it)
    }

    return res
}



class PublicSpeaker :OverridesJsonGenerator {
    val name: String
    val dataValues: Map<String, String>

    constructor(speakerInDb: SpeakerInDb) {
        this.name = speakerInDb.name
        this.dataValues = toPublicMap(speakerInDb.dataMap)
    }

    constructor(jsonObject: JsonObject) {
        name = jsonObject.requiredString("name")
        val dv:MutableMap<String,String> = mutableMapOf()
        for (key in jsonObject.keys()) {
            if (key == "name") {
                continue
            }
            dv[key] = jsonObject.requiredString(key)
        }
        dataValues = dv
    }

    override fun jsonValue(): JsonObject {
        val res = JsonObject().put("name",name)
        for (entry in dataValues.entries) {
            res.put(entry.key,entry.value)
        }
        return res
    }
}

private fun toZuluTimeString(localdateString: String): String {
    val localdate = LocalDateTime.parse(localdateString)
    val zonedDateTime = localdate.atZone(ZoneId.of("Europe/Oslo"))
    return zonedDateTime.withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_DATE_TIME)
}

class PublicTalk :OverridesJsonGenerator {


    val dataValues: Map<String, String>
    val speakers: List<PublicSpeaker>


    constructor(talkInDb: TalkInDb,speakersInDb:List<SpeakerInDb>) {
        dataValues = toPublicMap(talkInDb.dataMap)
        speakers = speakersInDb.map { PublicSpeaker(it) }
    }

    constructor(jsonObject: JsonObject) {
        val dv:MutableMap<String,String> = mutableMapOf()
        for (key in jsonObject.keys()) {
            if (key == "speakers") {
                continue
            }
            dv[key] = jsonObject.requiredString(key)
        }

        dataValues = dv
        speakers = jsonObject.requiredArray("speakers").objects { PublicSpeaker(it) }
    }

    override fun jsonValue(): JsonObject {
        val res = JsonObject()
        for (entry in dataValues.entries) {
            res.put(entry.key,entry.value)
        }
        res.put("speakers",JsonArray.fromNodeList(speakers.map { it.jsonValue() }))
        return res
    }


}