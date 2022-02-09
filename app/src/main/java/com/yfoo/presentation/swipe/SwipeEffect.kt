package com.yfoo.presentation.swipe

sealed class SwipeEffect {
    data class OpenUrl(val url: String) : SwipeEffect()
    data class OpenImage(val path: String) : SwipeEffect()
}