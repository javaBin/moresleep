package no.java.moresleep.talk

import no.java.moresleep.BaseTestClass
import no.java.moresleep.UserType
import no.java.moresleep.baseDataTestset
import no.java.moresleep.conference.CreateNewConference
import org.jsonbuddy.JsonString
import org.junit.jupiter.api.Test


class TalkTest:BaseTestClass() {

    @Test
    fun createSession() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(UserType.FULLACCESS, emptyMap()).id

        CreateNewSession(
                postedBy = "anders@java.no",
                status = SessionStatus.SUBMITTED.toString(),
                data = baseDataTestset
        ).execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))
    }
}