package com.ramitsuri.locationtracking.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun TintedIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    showProgress: Boolean = false,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        if (showProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
fun TintedIconButton(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    showProgress: Boolean = false,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        if (showProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
