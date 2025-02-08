package com.ramitsuri.locationtracking.wear.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ActionBuilders.AndroidActivity
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.wear.presentation.MainActivity

@OptIn(ExperimentalHorologistApi::class)
class TileRenderer(context: Context) :
    SingleTileLayoutRenderer<TileState, Unit>(context) {
    override fun renderTile(
        state: TileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
    ): LayoutElementBuilders.LayoutElement {
        return PrimaryLayout.Builder(deviceParameters).setResponsiveContentInsetEnabled(true)
            .setContent(
                MultiButtonLayout.Builder()
                    .build(),
            )
            .setPrimaryChipContent(
                CompactChip.Builder(
                    context,
                    context.getString(R.string.app_name),
                    launchActivityClickable(launchActivityAction()),
                    deviceParameters,
                )
                    .setChipColors(ChipColors.secondaryChipColors(theme))
                    .build(),
            )
            .build()
    }

    private fun launchActivityAction(action: String = MainActivity.ADD): AndroidActivity =
        AndroidActivity.Builder()
            .setPackageName(context.packageName)
            .setClassName(ACTIVITY)
            .addKeyToExtraMapping(
                MainActivity.EXTRA_KEY,
                ActionBuilders.stringExtra(action),
            )
            .build()

    private fun launchActivityClickable(activity: AndroidActivity): ModifiersBuilders.Clickable =
        ModifiersBuilders.Clickable.Builder()
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(activity)
                    .build(),
            )
            .build()

    companion object {
        private const val ACTIVITY =
            "com.ramitsuri.locationtracking.presentation.MainActivity"
    }
}
