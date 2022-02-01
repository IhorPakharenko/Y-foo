package com.yfoo.presentation.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.activity.ComponentActivity
import com.yfoo.presentation.utils.rememberWindowSizeClass
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowSizeClass = rememberWindowSizeClass()
            YfooApp(windowSize = windowSizeClass)
        }
    }
}