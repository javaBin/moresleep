package no.java.moresleep.conference

import no.java.moresleep.BaseTestClass
import no.java.moresleep.UserType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ConferenceTest:BaseTestClass() {
    @Test
    fun addReadConference() {
        val cnc = CreateNewConference(name = "JavaZone 2021",slug = "javazone2021")
        val conferenceid = cnc.execute(UserType.FULLACCESS,"/conference").id
        val allConferences:List<Conference> = ReadAllConferences().execute(UserType.FULLACCESS,"/conference").conferences
        Assertions.assertThat(allConferences).hasSize(1)
        Assertions.assertThat(allConferences[0].id).isEqualTo(conferenceid)
        Assertions.assertThat(allConferences[0].name).isEqualTo("JavaZone 2021")
        Assertions.assertThat(allConferences[0].slug).isEqualTo("javazone2021")

    }
}