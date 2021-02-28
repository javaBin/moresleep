package db.migration

import no.java.moresleep.ServiceExecutor
import no.java.moresleep.conference.ConferenceRepo
import no.java.moresleep.setTimestamp
import no.java.moresleep.talk.TalkRepo
import no.java.moresleep.util.DbMigrationStep
import org.jsonbuddy.JsonObject

class V3__UpdateIdOnPublic:DbMigrationStep() {
    override fun doMigrate() {
        val conferences = ConferenceRepo.allConferences()
        for (conference in conferences) {
            println("Updating conference ${conference.name}")
            val allTalks = TalkRepo.allTalksInForConference(conference.id)
            for (talk in allTalks) {
                if (talk.publicdata == null) {
                    continue
                }
                val res:JsonObject = talk.publicdata
                    .put("id",talk.id)
                    .put("sessionId",talk.id)
                    .put("conferenceId",talk.conferenceid)
                ServiceExecutor.connection().preparedStatement("update talk set publicdata = ? where id = ?") {
                    it.setString(1,res.toJson())
                    it.setString(2,talk.id)
                    it.executeUpdate()
                }
                ServiceExecutor.commit()
            }
        }
        println("Done V3migration")
    }
}