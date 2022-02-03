package com.yfoo.domain

data class Profile(
    val climateChangeCoins: Int,
    val unlockedFeatures: List<UnlockedFeature>,
) {
    enum class UnlockedFeature { ViewFavorites }
}