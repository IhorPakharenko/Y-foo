package com.yfoo.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class MviViewModel<StateType, OneShotEventsType>(emptyState: StateType) : ViewModel() {
    protected val _state = MutableStateFlow(emptyState)
    val state get() = _state.asStateFlow()

    protected val _oneShotEvents = Channel<OneShotEventsType>(Channel.BUFFERED)
    val oneShotEvents: Flow<OneShotEventsType> get() = _oneShotEvents.receiveAsFlow()
}