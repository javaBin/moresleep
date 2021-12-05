package no.java.moresleep.conference

import no.java.moresleep.BaseTestClass
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ConferenceTest:BaseTestClass() {
    @Test
    fun addReadConference() {
        val cnc = CreateNewConference(name = "JavaZone 2021",slug = "javazone2021")
        val conferenceid = cnc.execute(testFullAccessUser, emptyMap()).id
        val allConferences:List<Conference> = (ReadAllConferences().execute(testFullAccessUser, emptyMap()) as ReadAllConferencesResult).conferences
        Assertions.assertThat(allConferences).hasSize(1)
        Assertions.assertThat(allConferences[0].id).isEqualTo(conferenceid)
        Assertions.assertThat(allConferences[0].name).isEqualTo("JavaZone 2021")
        Assertions.assertThat(allConferences[0].slug).isEqualTo("javazone2021")
        Assertions.assertThat(allConferences[0].slottimes).isEqualTo("09:00,10:20,11:40,13:00,14:20,15:40,17:00,18:20")

        val allPubicConf:ReadAllConferencesPublicResult = ReadAllConferences().execute(testAnonUser, emptyMap()) as ReadAllConferencesPublicResult

        Assertions.assertThat(allPubicConf.conferences).hasSize(1)
        Assertions.assertThat(allPubicConf.conferences[0].stringValue("slottimes").isPresent).isFalse()
        Assertions.assertThat(allPubicConf.conferences[0].requiredString("id")).isEqualTo(conferenceid)
        Assertions.assertThat(allPubicConf.conferences[0].requiredString("name")).isEqualTo("JavaZone 2021")
        Assertions.assertThat(allPubicConf.conferences[0].requiredString("slug")).isEqualTo("javazone2021")

    }
}