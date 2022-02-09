package com.yfoo.presentation.main

sealed class MainEffect {
    object NavigateToSwipe : MainEffect()
    object NavigateToLiked : MainEffect()
    object NavigateToChat : MainEffect()
    object NavigateToProfile : MainEffect()
}