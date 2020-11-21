package no.java.moresleep.talk

import org.jsonbuddy.JsonNode
import org.jsonbuddy.JsonObject

class DataValue(@JvmField val privateData:Boolean=true,val value:JsonNode?=null) {
    constructor(valueObject:JsonObject):this(privateData = valueObject.booleanValue("privateData").orElse(true),value = valueObject.value("value").orElse(null))
}