package com.yfoo.presentation.main

sealed class MainUiAction {
    object SwipeClick : MainUiAction()
    object LikedClick : MainUiAction()
    object ChatClick : MainUiAction()
    object ProfileClick : MainUiAction()
}