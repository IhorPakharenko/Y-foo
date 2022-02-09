package com.yfoo.presentation.main

sealed class MainEvent {
    object SwipeClick : MainEvent()
    object LikedClick : MainEvent()
    object ChatClick : MainEvent()
    object ProfileClick : MainEvent()
}