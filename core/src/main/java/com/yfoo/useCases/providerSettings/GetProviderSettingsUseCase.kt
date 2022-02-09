package com.yfoo.useCases.providerSettings

import com.yfoo.domain.ImageProvider
import com.yfoo.domain.ProviderSettings

class GetProviderSettingsUseCase {
    suspend operator fun invoke(): List<ProviderSettings> {
        return ImageProvider.values().map {
            ProviderSettings(it, true)
        }
    }
}