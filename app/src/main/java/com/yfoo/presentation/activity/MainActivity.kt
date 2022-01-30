package com.yfoo.presentation.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Text("Hello Yfoo")
        }
    }
}