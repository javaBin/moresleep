package no.java.moresleep.talk

import no.java.moresleep.*
import no.java.moresleep.conference.CreateNewConference
import org.assertj.core.api.Assertions.assertThat
import org.jsonbuddy.JsonObject
import org.jsonbuddy.JsonString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletResponse


class TalkTest:BaseTestClass() {

    @Test
    fun createSession() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id

        val talkDetail = createNewSession().execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        assertThat(talkDetail.speakers).hasSize(1)
    }

    private fun createNewSession() = CreateNewSession(
        postedBy = "anders@java.no",
        status = SessionStatus.SUBMITTED.toString(),
        data = baseTalkDataTestset,
        speakers = listOf(SpeakerUpdate(name = "Anders Lastname", email = "anders@java.no", data = baseSpeakerDataTestset))
    )

    @Test
    fun updateValueInSession() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id

        val talkid = createNewSession().execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid))).id

        val talkDetail:TalkDetail = ReadOneSession().execute(testFullAccessUser, mapOf(Pair("id",talkid)))
        assertThat(talkDetail.id).isEqualTo(talkid)
        assertThat(talkDetail.data["abstract"]?.privateData).isFalse()
        assertThat(talkDetail.data["abstract"]?.value).isEqualTo(JsonString("Here is the abstract"))
        assertThat(talkDetail.data["outline"]?.privateData).isTrue()
        assertThat(talkDetail.data["outline"]?.value).isEqualTo(JsonString("This is an outline"))


        val updatedTalk:TalkDetail = UpdateSession(mapOf(Pair("abstract",DataValue(false,JsonString("Updated abstract")))))
            .execute(testFullAccessUser, mapOf(Pair("id",talkid)))

        assertThat(updatedTalk.id).isEqualTo(talkid)
        assertThat(updatedTalk.data["abstract"]?.privateData).isFalse()
        assertThat(updatedTalk.data["abstract"]?.value).isEqualTo(JsonString("Updated abstract"))
        assertThat(updatedTalk.data["outline"]?.privateData).isTrue()
        assertThat(updatedTalk.data["outline"]?.value).isEqualTo(JsonString("This is an outline"))

        assertThat(TalkRepo.updatesOnTalk(talkid)).hasSize(2)
    }

    private val darthAndLukeSpeakers = listOf(
        SpeakerUpdate(name = "Luke Skywalker", email = "luke@java.no", data = baseSpeakerDataTestset),
        SpeakerUpdate(name = "Darth Vader", email = "darth@java.no", data = baseSpeakerDataTestset),
    )

    @Test
    fun updateSpeakerOnTalk() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id

        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        val vaderId = talkDetail.speakers.first { it.name ==  "Darth Vader"}.id
        val lukeid = talkDetail.speakers.first { it.name ==  "Luke Skywalker"}.id

        val updatedTalk = UpdateSession(speakers = listOf(
            SpeakerUpdate(id = lukeid),
            SpeakerUpdate(id = vaderId,name = "Anakin Skywalker")
        )).execute(testFullAccessUser, mapOf(Pair("id",talkDetail.id)))

        assertThat(updatedTalk.speakers).hasSize(2)
        assertThat(updatedTalk.speakers.first { it.id == vaderId }.name).isEqualTo("Anakin Skywalker")
    }

    @Test
    fun addASpeakerToATalk() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id

        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = listOf(SpeakerUpdate(name = "Luke Skywalker", email = "luke@java.no", data = baseSpeakerDataTestset))
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        val lukeid = talkDetail.speakers.first { it.name ==  "Luke Skywalker"}.id

        val updatedTalk = UpdateSession(speakers = listOf(
            SpeakerUpdate(id = lukeid),
            SpeakerUpdate(name = "Darth Vader", email = "darth@java.no", data = baseSpeakerDataTestset)
        )).execute(testFullAccessUser, mapOf(Pair("id",talkDetail.id)))

        assertThat(updatedTalk.speakers).hasSize(2)



    }

    @Test
    fun shouldDeleteSpeaker() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id

        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        val vaderId = talkDetail.speakers.first { it.name ==  "Darth Vader"}.id
        val updatedTalk = UpdateSession(speakers = listOf(
            SpeakerUpdate(id = vaderId)
        )).execute(testFullAccessUser, mapOf(Pair("id",talkDetail.id)))

        assertThat(updatedTalk.speakers).hasSize(1)
    }

    @Test
    fun shouldListAllSessions() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id
        val talkDetail1:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))
        val data = mapOf(
            Pair("title", DataValue(privateData = false,value = JsonString("My cool talk title two"))),
            Pair("abstract", DataValue(privateData = false,value = JsonString("Here is the abstract"))),
            Pair("outline", DataValue(privateData = true,value = JsonString("This is an outline"))),
        )
        val talkDetail2:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = data,
            speakers = darthAndLukeSpeakers
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        val allTalks:AllTalks = ReadAllTalks().execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        assertThat(allTalks.sessions).hasSize(2)
        assertThat(allTalks.sessions.map { it.id }).contains(talkDetail1.id,talkDetail2.id)
        assertThat(allTalks.sessions.first { it.id == talkDetail1.id}.speakers).hasSize(2)
        assertThat(allTalks.sessions.first { it.id == talkDetail1.id}.speakers.map { it.id }).containsAll(talkDetail1.speakers.map { it.id })

        val talksBySubmitter = ReadTalksBySubmitter().execute(testFullAccessUser, mapOf(Pair("email","luke@java.no")))
        assertThat(talksBySubmitter.sessions).hasSize(2)

        val talksByPoster = ReadTalksBySubmitter().execute(testFullAccessUser, mapOf(Pair("email","anders@java.no")))
        assertThat(talksByPoster.sessions).hasSize(2)

    }

    @Test
    fun publishTalk() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id
        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        assertThat(talkDetail.sessionUpdates.hasUnpublishedChanges).isFalse()

        PublishTalk.doPublish(talkDetail.id,SessionStatus.APPROVED)
        val updatedTalk:TalkDetail = UpdateSession(mapOf(Pair("abstract",DataValue(false,JsonString("Updated abstract")))))
            .execute(testFullAccessUser, mapOf(Pair("id",talkDetail.id)))

        assertThat(updatedTalk.data["abstract"]!!.value!!.stringValue()).isEqualTo("Updated abstract")
        assertThat(updatedTalk.sessionUpdates.hasUnpublishedChanges).isTrue()
        assertThat(updatedTalk.sessionUpdates.oldValues).hasSize(1)
        assertThat(updatedTalk.sessionUpdates.oldValues[0].key).isEqualTo("abstract")
        assertThat(updatedTalk.sessionUpdates.oldValues[0].value).isEqualTo("Here is the abstract")

        val readTalks:AllPublicTalks = ReadAllPublicTalks().execute(testAnonUser, mapOf(Pair("slug","javazone2021")))
        val sessionArray = readTalks.asJsonObject().requiredArray("sessions")
        assertThat(sessionArray).hasSize(1)


        val ifUnmodified = LocalDateTime.now().plusHours(1).atZone(ZoneId.of("Europe/Oslo")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
        try {
            ReadAllPublicTalks().execute(testAnonUser, mapOf(Pair("slug","javazone2021"),Pair("If-Unmodified-Since",ifUnmodified)))
            fail("Expected request error")
        } catch (e:RequestError) {
            assertThat(e.httpError).isEqualTo(HttpServletResponse.SC_PRECONDITION_FAILED)
        }

    }

    @Test
    fun publishFromCake() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id

        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        UpdateSession(status = SessionStatus.APPROVED).execute(testFullAccessUser, mapOf(Pair("id",talkDetail.id)))

        val allPublicTalks:AllPublicTalks = ReadAllPublicTalks().execute(testAnonUser, mapOf(Pair("slug","javazone2021")))
        assertThat(allPublicTalks.asJsonObject().requiredArray("sessions")).hasSize(1)
    }

    @Test
    fun republish() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id

        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        UpdateSession(status = SessionStatus.APPROVED).execute(testFullAccessUser, mapOf(Pair("id",talkDetail.id)))

        val startTime:LocalDateTime = LocalDateTime.of(2021,12,8,10,20)
        val endTime:LocalDateTime = LocalDateTime.of(2021,12,8,11,20)
        val data = mapOf(
            Pair("startTime", DataValue(privateData = false, value = JsonString(startTime.toString()))),
            Pair("length", DataValue(privateData = false, value = JsonString("60"))),
            Pair("endTime", DataValue(privateData = false, value = JsonString(endTime.toString())))
        )

        UpdateSession(data = data).execute(testFullAccessUser, mapOf(Pair("id",talkDetail.id)))


        val publicTalksFromConference:List<PublicTalkInDb> = TalkRepo.publicTalksFromConference(conferenceid)
        assertThat(publicTalksFromConference).hasSize(1)
        assertThat(publicTalksFromConference[0].content.value("startTime")).isEmpty()


        UpdateSession(status = SessionStatus.APPROVED).execute(testFullAccessUser, mapOf(Pair("id",talkDetail.id)))

        val publicTalksFromConferenceAfter:List<PublicTalkInDb> = TalkRepo.publicTalksFromConference(conferenceid)
        assertThat(publicTalksFromConferenceAfter).hasSize(1)
        assertThat(publicTalksFromConferenceAfter[0].content.value("startTime")).isNotEmpty()
    }

    @Test
    fun talkWithSlot() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id
        val startTime:LocalDateTime = LocalDateTime.of(2021,12,8,10,20)
        val endTime:LocalDateTime = LocalDateTime.of(2021,12,8,11,20)
        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = mapOf(
                Pair("title", DataValue(privateData = false, value = JsonString("My cool talk title"))),
                Pair("abstract", DataValue(privateData = false, value = JsonString("Here is the abstract"))),
                Pair("outline", DataValue(privateData = true, value = JsonString("This is an outline"))),
                Pair("startTime", DataValue(privateData = false, value = JsonString(startTime.toString()))),
                Pair("length", DataValue(privateData = false, value = JsonString("60"))),
                Pair("endTime", DataValue(privateData = false, value = JsonString(endTime.toString()))),
            ),
            speakers = darthAndLukeSpeakers
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        PublishTalk.doPublish(talkDetail.id,SessionStatus.APPROVED)

        val allPublicTalks:AllPublicTalks = ReadAllPublicTalks().execute(testAnonUser, mapOf(Pair("slug","javazone2021")))
        val talkobj:JsonObject = allPublicTalks.asJsonObject().requiredArray("sessions").get(0,JsonObject::class.java)
        assertThat(talkobj.requiredString("startTime")).isEqualTo("2021-12-08T10:20")
        assertThat(talkobj.requiredString("startSlot")).isEqualTo("2021-12-08T10:20")
        assertThat(talkobj.requiredString("startTimeZulu")).isEqualTo("2021-12-08T09:20:00Z")
        assertThat(talkobj.requiredString("startSlotZulu")).isEqualTo("2021-12-08T09:20:00Z")
        assertThat(talkobj.requiredString("endTime")).isEqualTo("2021-12-08T11:20")
        assertThat(talkobj.requiredString("endTimeZulu")).isEqualTo("2021-12-08T10:20:00Z")


    }

    @Test
    fun talkWithStartSlot() {
        Setup.setValue(SetupValue.CONFIG_SLOTS,"true")
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(testFullAccessUser, emptyMap()).id
        val startTime:LocalDateTime = LocalDateTime.of(2021,12,8,10,30)
        val endTime:LocalDateTime = LocalDateTime.of(2021,12,8,10,40)
        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders2@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = mapOf(
                Pair("title", DataValue(privateData = false, value = JsonString("My Light"))),
                Pair("abstract", DataValue(privateData = false, value = JsonString("Here is the abstract"))),
                Pair("outline", DataValue(privateData = true, value = JsonString("This is an outline"))),
                Pair("startTime", DataValue(privateData = false, value = JsonString(startTime.toString()))),
                Pair("length", DataValue(privateData = false, value = JsonString("10"))),
                Pair("endTime", DataValue(privateData = false, value = JsonString(endTime.toString()))),
            ),
            speakers = darthAndLukeSpeakers
        ).execute(testFullAccessUser, mapOf(Pair("conferenceId",conferenceid)))

        PublishTalk.doPublish(talkDetail.id,SessionStatus.APPROVED)

        PublicTalkReadService.clearCache()
        val allPublicTalks:AllPublicTalks = ReadAllPublicTalks().execute(testAnonUser, mapOf(Pair("slug","javazone2021")))
        val talkArr = allPublicTalks.asJsonObject().requiredArray("sessions")
        assertThat(talkArr).hasSize(1)

        val talkobj:JsonObject = talkArr.get(0,JsonObject::class.java)
        assertThat(talkobj.requiredString("startTime")).isEqualTo("2021-12-08T10:30")
        assertThat(talkobj.requiredString("startTimeZulu")).isEqualTo("2021-12-08T09:30:00Z")
        assertThat(talkobj.requiredString("endTime")).isEqualTo("2021-12-08T10:40")
        assertThat(talkobj.requiredString("endTimeZulu")).isEqualTo("2021-12-08T09:40:00Z")

        assertThat(talkobj.requiredString("startSlot")).isEqualTo("2021-12-08T10:20")
        assertThat(talkobj.requiredString("startSlotZulu")).isEqualTo("2021-12-08T09:20:00Z")
    }


}