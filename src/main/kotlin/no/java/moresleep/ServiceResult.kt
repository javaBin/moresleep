package no.java.moresleep

import org.jsonbuddy.JsonObject
import org.jsonbuddy.pojo.JsonGenerator

abstract class ServiceResult {
   open fun asJsonObject():JsonObject = JsonGenerator.generate(this) as JsonObject

}