package com.yfoo.domain

import kotlinx.datetime.Instant

data class ChatMessage(
    val message: String,
    val timeSent: Instant,
)