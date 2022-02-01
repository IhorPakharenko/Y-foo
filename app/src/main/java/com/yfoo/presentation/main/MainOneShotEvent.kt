package com.yfoo.presentation.main

sealed class MainOneShotEvent {
    object NavigateToSwipe : MainOneShotEvent()
    object NavigateToLiked : MainOneShotEvent()
    object NavigateToChat : MainOneShotEvent()
    object NavigateToProfile : MainOneShotEvent()
}