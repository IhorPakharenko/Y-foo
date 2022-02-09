package com.yfoo.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SignalWifiStatusbarConnectedNoInternet4
import com.yfoo.R

val Throwable.isNetworkRelated get() = false //TODO check what errors are network related

val Throwable.messageForUi
    get() = if (isNetworkRelated) {
        R.string.check_your_internet
    } else {
        R.string.try_again_later
    }

val Throwable.iconForUi
    get() = if (isNetworkRelated) {
        Icons.Filled.SignalWifiStatusbarConnectedNoInternet4
    } else {
        Icons.Filled.ErrorOutline
    }