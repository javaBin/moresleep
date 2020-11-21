package no.java.moresleep.talk

import no.java.moresleep.BaseTestClass
import no.java.moresleep.UserType
import no.java.moresleep.conference.CreateNewConference
import org.jsonbuddy.JsonString
import org.junit.jupiter.api.Test

class TalkTest:BaseTestClass() {

    @Test
    fun createSession() {
        val conferenceid = CreateNewConference(name = "JavaZone 2021", slug = "javazone2021").execute(UserType.FULLACCESS, emptyMap()).id
        val data:Map<String,DataValue> = mapOf(
                Pair("title",DataValue(privateData = false,value = JsonString("My cool talk title"))),
                Pair("abstract",DataValue(privateData = false,value = JsonString("Here is the abstract"))),
                Pair("outline",DataValue(privateData = true,value = JsonString("This is an outline"))),
        )
        CreateNewSession(
                postedBy = "anders@java.no",
                status = SessionStatus.SUBMITTED.toString(),
                data = data
        ).execute(UserType.FULLACCESS, mapOf(Pair("conferenceId",conferenceid)))
    }
}