package com.yfoo.framework.di

import com.yfoo.useCases.providerSettings.GetProviderSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ProviderSettingsModule {
    @Provides
    fun provideGetProviderSettingsUseCase() = GetProviderSettingsUseCase()
}