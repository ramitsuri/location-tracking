package com.ramitsuri.locationtracking.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = wearColorPalette,
        typography = Typography,
        content = content,
    )
}
