package com.yfoo.presentation.main

import androidx.lifecycle.viewModelScope
import com.yfoo.presentation.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : MviViewModel<MainState, MainOneShotEvent>(MainState()) {
    fun onAction(action: MainUiAction) {
        viewModelScope.launch {
            when (action) {
                MainUiAction.SwipeClick -> _oneShotEvents.send(MainOneShotEvent.NavigateToSwipe)
                MainUiAction.LikedClick -> _oneShotEvents.send(MainOneShotEvent.NavigateToLiked)
                MainUiAction.ChatClick -> _oneShotEvents.send(MainOneShotEvent.NavigateToChat)
                MainUiAction.ProfileClick -> _oneShotEvents.send(MainOneShotEvent.NavigateToProfile)
            }
        }
    }
}

