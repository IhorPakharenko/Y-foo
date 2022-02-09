package com.yfoo.presentation.swipe

import androidx.lifecycle.viewModelScope
import com.yfoo.domain.Card
import com.yfoo.domain.ImageSource
import com.yfoo.presentation.MviViewModel
import com.yfoo.useCases.card.AddFavoriteCardUseCase
import com.yfoo.useCases.card.GetCardsUseCase
import com.yfoo.useCases.providerSettings.GetProviderSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val addFavoriteCardUseCase: AddFavoriteCardUseCase,
    private val getProviderSettingsUseCase: GetProviderSettingsUseCase,
) : MviViewModel<SwipeState, SwipeEffect, SwipeIntent>(SwipeState()) {
    init {
        viewModelScope.launch {
            launch {
                //TODO what if less than 3 cards are received in getCardsUseCase?
                state
                    .map { (it.content as? SwipeState.Content.Cards)?.value ?: emptyList() }
                    .map { it.size < 3 }
                    .distinctUntilChanged()
                    .collect {
                        val newCards = getCardsUseCase()
                        appendCards(newCards)
                    }
            }
            launch {
                //TODO observe and add cards here
            }
            launch {
                val providerSettings = getProviderSettingsUseCase().map { providerSetting ->
                    SwipeState.ProviderSetting(
                        provider = providerSetting.provider,
                        isChecked = providerSetting.isEnabled,
                        isToggleable = true,
                    )
                }
                setState {
                    copy(providers = providerSettings)
                }
            }
        }
    }

    override suspend fun handleIntent(intent: SwipeIntent) {
        when (intent) {
            is SwipeIntent.Like -> like(intent.card)
            is SwipeIntent.Dislike -> dislike(intent.card)
            is SwipeIntent.ViewProvider -> viewProvider(intent.card)
            is SwipeIntent.ViewImage -> viewImage(intent.card)
            SwipeIntent.RevertLastCard -> revertLastCard()
            is SwipeIntent.SetSettingsVisibility -> setSettingsVisibility(intent.isVisible)
            is SwipeIntent.ToggleProvider -> toggleProvider(
                intent.provider,
                intent.isChecked
            )
        }
    }

    private suspend fun like(card: Card) {
        removeCard(card)
        addFavoriteCardUseCase(card)
    }

    private fun dislike(card: Card) {
        removeCard(card)
    }

    private fun viewProvider(card: Card) {
        setEffect {
            SwipeEffect.OpenUrl(card.provider.url)
        }
    }

    private fun viewImage(card: Card) {
        setEffect {
            when (card.source.type) {
                ImageSource.Type.Url -> SwipeEffect.OpenUrl(card.source.value)
                ImageSource.Type.Path -> SwipeEffect.OpenImage(card.source.value)
            }
        }
    }

    private fun removeCard(card: Card) {
        setState {
            if (content !is SwipeState.Content.Cards) return@setState this
            copy(
                content = SwipeState.Content.Cards(content.value - card),
                lastRemovedCard = card,
            )
        }
    }

    private fun revertLastCard() {
        setState {
            if (lastRemovedCard == null) return@setState this
            val currentCards =
                if (content is SwipeState.Content.Cards) content.value else emptyList()
            copy(
                content = SwipeState.Content.Cards(listOf(lastRemovedCard) + currentCards),
                lastRemovedCard = null,
            )
        }
    }

    private fun appendCards(newCards: List<Card>) {
        setState {
            if (newCards.isEmpty()) return@setState this
            val currentCards =
                if (content is SwipeState.Content.Cards) content.value else emptyList()
            copy(content = SwipeState.Content.Cards(currentCards + newCards))
        }
    }

    private fun setSettingsVisibility(isVisible: Boolean) {
        setState { copy(areSettingsVisible = isVisible) }
    }

    private fun toggleProvider(setting: SwipeState.ProviderSetting, isChecked: Boolean) {
        setState {
            val updatedProviders = providers.setChecked(setting, isChecked)

            //TODO should do this in an observer to support first init and changes from the db
            val finalProviders = when (updatedProviders.count { it.isChecked }) {
                0 -> providers
                1 -> updatedProviders.setToggleableExcept(
                    exceptProviderSetting = updatedProviders.first { it.provider == setting.provider },
                    isToggleable = false,
                )
                else -> updatedProviders.setToggleable(true)
            }

            copy(providers = finalProviders)
        }
    }

    private fun List<SwipeState.ProviderSetting>.setToggleableExcept(
        exceptProviderSetting: SwipeState.ProviderSetting,
        isToggleable: Boolean,
    ): List<SwipeState.ProviderSetting> = map { providerSetting ->
        if (providerSetting == exceptProviderSetting) {
            providerSetting.copy(isToggleable = !isToggleable)
        } else {
            providerSetting.copy(isToggleable = isToggleable)
        }
    }

    private fun List<SwipeState.ProviderSetting>.setToggleable(
        isToggleable: Boolean,
    ): List<SwipeState.ProviderSetting> = map { entry ->
        entry.copy(isToggleable = isToggleable)
    }

    private fun List<SwipeState.ProviderSetting>.setChecked(
        changedProviderSetting: SwipeState.ProviderSetting,
        isChecked: Boolean,
    ): List<SwipeState.ProviderSetting> = changeFirst(changedProviderSetting) {
        it.copy(isChecked = isChecked)
    }

    private fun <T> List<T>.changeFirst(
        item: T,
        operation: (T) -> T,
    ): List<T> = map { entry ->
        if (entry == item) {
            operation(entry)
        } else {
            entry
        }
    }
}