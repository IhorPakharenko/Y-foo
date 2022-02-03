package com.yfoo.domain

data class Card(
    val provider: Provider,
    val imageSource: ImageSource,
) {
    enum class Provider {
        ThisAnimeDoesNotExist,
        ThisWaifuDoesNotExist,
        ThisCatDoesNotExist,
    }

    sealed class ImageSource {
        data class Url(val url: String) : ImageSource()
        data class InternalCache(val id: String) : ImageSource()
    }
}