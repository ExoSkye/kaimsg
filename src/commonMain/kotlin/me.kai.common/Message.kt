package me.kai.common

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Message(val content: String, val timePosted: Instant, val userID: ULong)