package db.migration

import no.java.moresleep.ServiceExecutor
import no.java.moresleep.conference.ConferenceRepo
import no.java.moresleep.talk.*
import no.java.moresleep.util.DbMigrationStep
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject

class V5__SpeakerBioPublic:DbMigrationStep() {
    private fun JsonObject.objectOptional(key:String) = this.objectValue(key).orElse(null)

    override fun doMigrate() {
        val conferences = ConferenceRepo.allConferences()
        val jz2021Conf = conferences.firstOrNull { it.slug == "javazone_2021" }?:return
        val allTalksInForConference = TalkRepo.allTalksInForConference(jz2021Conf.id)
        val allSpeakersInConference = SpeakerRepo.allSpeakersInConference(jz2021Conf.id)

        for (talk:TalkInDb in allTalksInForConference) {
            val speakersForTalk = allSpeakersInConference.filter { it.talkId == talk.id }
            for (speaker:SpeakerInDb in speakersForTalk) {
                val bioObj = speaker.data.objectOptional("bio") ?: continue
                bioObj.put("privateData",false)
                SpeakerRepo.updateSpeaker(speaker.id,speaker.name,speaker.email,speaker.data)
            }
            if (talk.publicdata != null) {
                PublishTalk.doPublish(talk.id,talk.status)
            }
            ServiceExecutor.commit()
        }
    }
}