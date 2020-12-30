package no.java.moresleep.talk

class OldValue(val key:String,val value:String)

class SessionUpdates(talkInDb: TalkInDb?,speakers:List<SpeakerInDb>) {
    val speakerUpdates:List<String> = emptyList()
    @JvmField val hasUnpublishedChanges:Boolean
    val oldValues:List<OldValue>

    init {
        if  (talkInDb?.publicdata == null || talkInDb.publishedAt == null || talkInDb.publishedAt == talkInDb.lastUpdated)  {
            oldValues = emptyList()
            hasUnpublishedChanges = false
        } else {
            hasUnpublishedChanges = true

            val currentVersion = PublicTalk(talkInDb,speakers)
            val previousVersion = PublicTalk(talkInDb.publicdata)
            val compOldValues:MutableList<OldValue> = mutableListOf()

            for (previousEntry in previousVersion.dataValues.entries) {
                if (currentVersion.dataValues[previousEntry.key] != previousEntry.value) {
                    compOldValues.add(OldValue(previousEntry.key,previousEntry.value))
                }
            }

            oldValues = compOldValues
        }


    }
}