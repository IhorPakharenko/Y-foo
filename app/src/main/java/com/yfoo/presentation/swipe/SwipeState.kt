package com.yfoo.presentation.swipe

import com.yfoo.domain.Card
import com.yfoo.domain.ImageProvider

data class SwipeState(
    val content: Content = Content.Cards(emptyList()),
    val lastRemovedCard: Card? = null,
    val areSettingsVisible: Boolean = false,
    val providers: List<ProviderSetting> = emptyList(),
) {
    val canRevertCard = lastRemovedCard != null

    sealed class Content {
        data class Cards(val value: List<Card>) : SwipeState.Content()
        data class Error(val value: Throwable) : SwipeState.Content()
    }

    data class ProviderSetting(
        val provider: ImageProvider,
        val isChecked: Boolean,
        val isToggleable: Boolean,
    )
}
