package no.java.moresleep.conference

import no.java.moresleep.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class UpdateConference(val slottimes:String?=null) :Command {

    private fun validateSlottimes():String {
        if (slottimes == null) {
            throw BadRequest("Missing slottimes")
        }
        val timeformat = DateTimeFormatter.ofPattern("HH:mm")
        val readTimes:List<LocalTime> = slottimes.split(",").map {
            try {
                LocalTime.parse(it,timeformat)
            } catch (e:Exception) {
                throw BadRequest("Illegal time $it")
            }
        }
        var latest:LocalTime? = null
        for (time in readTimes) {
            if (latest != null && !time.isAfter(latest)) {
                throw BadRequest("Times not in order")
            }
            latest = time
        }
        return slottimes
    }

    override fun execute(systemUser: SystemUser, parameters: Map<String, String>): ServiceResult {
        val givenSlottimes:String = validateSlottimes()
        val conference:Conference = parameters["id"]?.let {  ConferenceRepo.oneConference(it)}?:throw BadRequest("Unknown conference ${parameters["id"]}")

        TODO("Not yet implemented")
    }

    override val requiredAccess: UserType = UserType.FULLACCESS
}