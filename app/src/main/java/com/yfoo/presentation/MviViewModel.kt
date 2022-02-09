package com.yfoo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class MviViewModel<StateType, EffectType, IntentsType>(
    emptyState: StateType
) : ViewModel() {
    private val _state = MutableStateFlow(emptyState)
    val state get() = _state.asStateFlow()

    private val _effect = Channel<EffectType>(Channel.BUFFERED)
    val effect: Flow<EffectType> get() = _effect.receiveAsFlow()

    private val _intent: MutableSharedFlow<IntentsType> = MutableSharedFlow()
    val intent = _intent.asSharedFlow()

    init {
        subscribeToIntents()
    }

    abstract suspend fun handleIntent(intent: IntentsType)

    fun onIntent(event: IntentsType) {
        viewModelScope.launch { _intent.emit(event) }
    }

    protected fun setState(reduce: StateType.() -> StateType) {
        _state.update(reduce)
    }

    //TODO "set" doesn't sound right here
    protected fun setEffect(builder: () -> EffectType) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    private fun subscribeToIntents() {
        viewModelScope.launch {
            intent.collect {
                handleIntent(it)
            }
        }
    }
}