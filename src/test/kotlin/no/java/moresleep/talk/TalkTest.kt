package no.java.moresleep.talk

import no.java.moresleep.BaseTestClass
import no.java.moresleep.UserType
import no.java.moresleep.baseSpeakerDataTestset
import no.java.moresleep.baseTalkDataTestset
import no.java.moresleep.conference.CreateNewConference
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.jsonbuddy.JsonString
import org.junit.jupiter.api.Test


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

}