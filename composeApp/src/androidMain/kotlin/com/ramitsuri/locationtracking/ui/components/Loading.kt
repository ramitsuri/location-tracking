package com.ramitsuri.locationtracking.ui.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun Loading() {
    Dialog(
        onDismissRequest = { },
        properties =
        DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
        ),
    ) {
        CircularProgressIndicator()
    }
}
