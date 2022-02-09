package com.yfoo.presentation.swipe

import com.yfoo.domain.Card

sealed class SwipeIntent {
    data class Like(val card: Card) : SwipeIntent()
    data class Dislike(val card: Card) : SwipeIntent()
    data class ViewProvider(val card: Card) : SwipeIntent()
    data class ViewImage(val card: Card) : SwipeIntent()
    object RevertLastCard : SwipeIntent()
    data class SetSettingsVisibility(val isVisible: Boolean) : SwipeIntent()
    data class ToggleProvider(val provider: SwipeState.ProviderSetting, val isChecked: Boolean) :
        SwipeIntent()
}