package no.java.moresleep.talk

import no.java.moresleep.BadRequest



class SpeakerUpdate(val id:String?=null,val name:String?=null,val email:String?=null,val data: Map<String,DataValue>?=null) {
    fun validateForCreation() {

    }

    fun addToDb(sessionId:String,conferenceId:String):Speaker {
        if (this.name.isNullOrEmpty()) {
            throw BadRequest("Missing name in speaker")
        }
        if (this.email.isNullOrEmpty()) {
            throw BadRequest("Missing email in speaker")
        }
        val speakerid = SpeakerRepo.addSpeaker(sessionId,conferenceId,this.name,this.email, toDataObject(this.data))
        return Speaker(
                        id = speakerid,
                        name = this.name,
                        email = this.email,
                        data = this.data?: emptyMap()
                )
    }
}