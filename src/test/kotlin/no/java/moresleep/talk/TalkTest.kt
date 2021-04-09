package no.java.moresleep.talk

import no.java.moresleep.*
import no.java.moresleep.conference.CreateNewConference
import org.assertj.core.api.Assertions.assertThat
import org.jsonbuddy.JsonString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletResponse


class TalkTest:BaseTestClass() {

    @Test
    fun createSession() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(UserType.FULLACCESS, emptyMap()).id

        val talkDetail = createNewSession().execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))

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
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(UserType.FULLACCESS, emptyMap()).id

        val talkid = createNewSession().execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid))).id

        val talkDetail:TalkDetail = ReadOneSession().execute(UserType.FULLACCESS, mapOf(Pair("id",talkid)))
        assertThat(talkDetail.id).isEqualTo(talkid)
        assertThat(talkDetail.data["abstract"]?.privateData).isFalse()
        assertThat(talkDetail.data["abstract"]?.value).isEqualTo(JsonString("Here is the abstract"))
        assertThat(talkDetail.data["outline"]?.privateData).isTrue()
        assertThat(talkDetail.data["outline"]?.value).isEqualTo(JsonString("This is an outline"))


        val updatedTalk:TalkDetail = UpdateSession(mapOf(Pair("abstract",DataValue(false,JsonString("Updated abstract")))))
            .execute(UserType.FULLACCESS, mapOf(Pair("id",talkid)))

        assertThat(updatedTalk.id).isEqualTo(talkid)
        assertThat(updatedTalk.data["abstract"]?.privateData).isFalse()
        assertThat(updatedTalk.data["abstract"]?.value).isEqualTo(JsonString("Updated abstract"))
        assertThat(updatedTalk.data["outline"]?.privateData).isTrue()
        assertThat(updatedTalk.data["outline"]?.value).isEqualTo(JsonString("This is an outline"))
    }

    private val darthAndLukeSpeakers = listOf(
        SpeakerUpdate(name = "Luke Skywalker", email = "luke@java.no", data = baseSpeakerDataTestset),
        SpeakerUpdate(name = "Darth Vader", email = "darth@java.no", data = baseSpeakerDataTestset),
    )

    @Test
    fun updateSpeakerOnTalk() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(UserType.FULLACCESS, emptyMap()).id

        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))

        val vaderId = talkDetail.speakers.first { it.name ==  "Darth Vader"}.id
        val lukeid = talkDetail.speakers.first { it.name ==  "Luke Skywalker"}.id

        val updatedTalk = UpdateSession(speakers = listOf(
            SpeakerUpdate(id = lukeid),
            SpeakerUpdate(id = vaderId,name = "Anakin Skywalker")
        )).execute(UserType.FULLACCESS, mapOf(Pair("id",talkDetail.id)))

        assertThat(updatedTalk.speakers).hasSize(2)
        assertThat(updatedTalk.speakers.first { it.id == vaderId }.name).isEqualTo("Anakin Skywalker")
    }

    @Test
    fun addASpeakerToATalk() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(UserType.FULLACCESS, emptyMap()).id

        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = listOf(SpeakerUpdate(name = "Luke Skywalker", email = "luke@java.no", data = baseSpeakerDataTestset))
        ).execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))

        val lukeid = talkDetail.speakers.first { it.name ==  "Luke Skywalker"}.id

        val updatedTalk = UpdateSession(speakers = listOf(
            SpeakerUpdate(id = lukeid),
            SpeakerUpdate(name = "Darth Vader", email = "darth@java.no", data = baseSpeakerDataTestset)
        )).execute(UserType.FULLACCESS, mapOf(Pair("id",talkDetail.id)))

        assertThat(updatedTalk.speakers).hasSize(2)



    }

    @Test
    fun shouldDeleteSpeaker() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(UserType.FULLACCESS, emptyMap()).id

        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))

        val vaderId = talkDetail.speakers.first { it.name ==  "Darth Vader"}.id
        val updatedTalk = UpdateSession(speakers = listOf(
            SpeakerUpdate(id = vaderId)
        )).execute(UserType.FULLACCESS, mapOf(Pair("id",talkDetail.id)))

        assertThat(updatedTalk.speakers).hasSize(1)
    }

    @Test
    fun shouldListAllSessions() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(UserType.FULLACCESS, emptyMap()).id
        val talkDetail1:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))
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
        ).execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))

        val allTalks:AllTalks = ReadAllTalks().execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))

        assertThat(allTalks.sessions).hasSize(2)
        assertThat(allTalks.sessions.map { it.id }).contains(talkDetail1.id,talkDetail2.id)
        assertThat(allTalks.sessions.first { it.id == talkDetail1.id}.speakers).hasSize(2)
        assertThat(allTalks.sessions.first { it.id == talkDetail1.id}.speakers.map { it.id }).containsAll(talkDetail1.speakers.map { it.id })

        val talksBySubmitter = ReadTalksBySubmitter().execute(UserType.FULLACCESS, mapOf(Pair("email","luke@java.no")))
        assertThat(talksBySubmitter.sessions).hasSize(2)

        val talksByPoster = ReadTalksBySubmitter().execute(UserType.FULLACCESS, mapOf(Pair("email","anders@java.no")))
        assertThat(talksByPoster.sessions).hasSize(2)

    }

    @Test
    fun publishTalk() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(UserType.FULLACCESS, emptyMap()).id
        val talkDetail:TalkDetail = CreateNewSession(
            postedBy = "anders@java.no",
            status = SessionStatus.SUBMITTED.toString(),
            data = baseTalkDataTestset,
            speakers = darthAndLukeSpeakers
        ).execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))

        assertThat(talkDetail.sessionUpdates.hasUnpublishedChanges).isFalse()

        PublishTalk().execute(UserType.FULLACCESS, mapOf(Pair("id",talkDetail.id)))
        val updatedTalk:TalkDetail = UpdateSession(mapOf(Pair("abstract",DataValue(false,JsonString("Updated abstract")))))
            .execute(UserType.FULLACCESS, mapOf(Pair("id",talkDetail.id)))

        assertThat(updatedTalk.data["abstract"]!!.value!!.stringValue()).isEqualTo("Updated abstract")
        assertThat(updatedTalk.sessionUpdates.hasUnpublishedChanges).isTrue()
        assertThat(updatedTalk.sessionUpdates.oldValues).hasSize(1)
        assertThat(updatedTalk.sessionUpdates.oldValues[0].key).isEqualTo("abstract")
        assertThat(updatedTalk.sessionUpdates.oldValues[0].value).isEqualTo("Here is the abstract")

        val readTalks:AllPublicTalks = ReadAllPublicTalks().execute(UserType.ANONYMOUS, mapOf(Pair("slug","javazone2021")))
        val sessionArray = readTalks.asJsonObject().requiredArray("sessions")
        assertThat(sessionArray).hasSize(1)


        val ifUnmodified = LocalDateTime.now().plusHours(1).atZone(ZoneId.of("Europe/Oslo")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
        try {
            ReadAllPublicTalks().execute(UserType.ANONYMOUS, mapOf(Pair("slug","javazone2021"),Pair("If-Unmodified-Since",ifUnmodified)))
            fail("Expected request error")
        } catch (e:RequestError) {
            assertThat(e.httpError).isEqualTo(HttpServletResponse.SC_PRECONDITION_FAILED)
        }



    }
}