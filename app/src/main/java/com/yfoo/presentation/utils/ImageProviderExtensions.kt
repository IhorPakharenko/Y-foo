package com.yfoo.presentation.utils

import androidx.annotation.StringRes
import com.yfoo.R
import com.yfoo.domain.ImageProvider

val ImageProvider.nameRes: Int
    @StringRes get() = when (this) {
        ImageProvider.ThisAnimeDoesNotExist -> R.string.this_anime_does_not_exist_site_name
        ImageProvider.ThisWaifuDoesNotExist -> R.string.this_waifu_does_not_exist_site_name
        ImageProvider.ThisCatDoesNotExist -> R.string.this_cat_does_not_exist_site_name
    }