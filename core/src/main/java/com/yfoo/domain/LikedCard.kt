package com.yfoo.domain

import kotlinx.datetime.Instant

data class LikedCard(
    val provider: ImageProvider,
    val source: String,
    val timeLiked: Instant,
)