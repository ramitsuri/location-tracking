package com.ramitsuri.locationtracking.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }

    companion object {
        const val EXTRA_KEY = "EXTRA_KEY"
        const val ADD = "ADD_JOURNAL_ENTRY"
    }
}
