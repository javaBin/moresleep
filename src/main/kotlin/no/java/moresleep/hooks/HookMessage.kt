package no.java.moresleep.hooks

import org.jsonbuddy.JsonObject
import java.util.UUID

enum class HookMessageType {
    ADDED_TALK, CHANGED_TALK, ADDED_CONFERENCE, PUBLISH_UOPDATES
}

data class HookMessage(
    val type:HookMessageType,
    val reference:String,
) {
    val messageId:String = UUID.randomUUID().toString()
}