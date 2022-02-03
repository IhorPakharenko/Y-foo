package com.yfoo.framework.di

import com.yfoo.useCases.card.AddFavoriteCardUseCase
import com.yfoo.useCases.card.GetCardsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object CardModule {
    @Provides
    fun provideGetCardsUseCase() = GetCardsUseCase()

    @Provides
    fun provideAddFavoriteCardUseCase() = AddFavoriteCardUseCase()
}