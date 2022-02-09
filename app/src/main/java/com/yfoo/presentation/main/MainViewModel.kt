package com.yfoo.presentation.main

import com.yfoo.presentation.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() :
    MviViewModel<MainState, MainEffect, MainEvent>(MainState()) {
    override suspend fun handleIntent(intent: MainEvent) {
        when (intent) {
            MainEvent.SwipeClick -> setEffect { MainEffect.NavigateToSwipe }
            MainEvent.LikedClick -> setEffect { MainEffect.NavigateToLiked }
            MainEvent.ChatClick -> setEffect { MainEffect.NavigateToChat }
            MainEvent.ProfileClick -> setEffect { MainEffect.NavigateToProfile }
        }
    }
}

