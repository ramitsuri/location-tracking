package com.ramitsuri.locationtracking.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ramitsuri.locationtracking.R

@Composable
fun HomeScreen(
    viewState: HomeViewState,
    modifier: Modifier = Modifier,
    onNavToSettings: () -> Unit,
    onNavToAppSettings: () -> Unit,
    onSingleLocation: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = onNavToSettings) {
                Icon(Icons.Default.Settings, null)
            }
        }
        val count = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.locations))
            }
            append(viewState.numOfLocations.toString())
        }
        Text(text = count)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSingleLocation,
        ) {
            Text(stringResource(R.string.single_location))
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (viewState.notGrantedPermissions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(3f)) {
                    Text(stringResource(R.string.not_granted_permissions))
                    viewState.notGrantedPermissions
                        .forEach {
                            Text(it.name)
                        }
                }
                IconButton(
                    onClick = onNavToAppSettings,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                }
            }
        }
    }
}
