package com.yfoo.presentation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object Intents {
    fun toUrl(
        context: Context,
        url: String,
    ) = context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}