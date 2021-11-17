package no.java.moresleep.talk

import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import org.jsonbuddy.JsonString
import org.jsonbuddy.pojo.OverridesJsonGenerator
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val slotStarts:List<LocalTime> = listOf(
    LocalTime.of(9,0),
    LocalTime.of(10,20),
    LocalTime.of(11,40),
    LocalTime.of(13,0),
    LocalTime.of(14,20),
    LocalTime.of(15,40),
    LocalTime.of(17,0),
    LocalTime.of(18,20),
)

private fun toStartSlot(startTimeStr:String?, endTimeStr:String?):String? {
    if (startTimeStr == null || endTimeStr == null) {
        return null
    }
    val startTime:LocalDateTime = LocalDateTime.parse(startTimeStr)
    val endTime:LocalDateTime = LocalDateTime.parse(endTimeStr)

    if (Duration.between(startTime,endTime).abs().seconds > 21L*60L) {
        return startTimeStr
    }
    val startTimeTime = startTime.toLocalTime()
    val slotStartTime:LocalTime = slotStarts.filter { (it.hour == startTime.hour && it.minute == startTime.minute) || it.isBefore(startTimeTime) }.maxOf { it }

    val slotStart = startTime.withHour(slotStartTime.hour).withMinute(slotStartTime.minute).withSecond(0).withNano(0)
    return slotStart.toString()
}

private fun toPublicMap(talkInDb: TalkInDb):Map<String,String> {
    val res = toPublicMap(talkInDb.dataMap)
    res["startTime"]?.let {
        print("input start $it")
        val toZuluTimeString = toZuluTimeString(it)
        res["startTimeZulu"] = toZuluTimeString
    }
    res["endTime"]?.let {
        res["endTimeZulu"] = toZuluTimeString(it)
    }
    res["id"] = talkInDb.id;
    res["sessionId"] = talkInDb.id
    res["conferenceId"] = talkInDb.conferenceid

    toStartSlot(res["startTime"],res["endTime"])?.let {
        res["startSlot"] = it
        res["startSlotZulu"] = toZuluTimeString(it)
    }

    return res

}

private fun toPublicMap(dataMap:Map<String,DataValue>):MutableMap<String,String> {
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
        dataValues = toPublicMap(talkInDb)
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