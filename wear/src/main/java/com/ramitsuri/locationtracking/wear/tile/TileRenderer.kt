package com.ramitsuri.locationtracking.wear.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ActionBuilders.AndroidActivity
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.ButtonDefaults
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.ui.label
import com.ramitsuri.locationtracking.wear.presentation.MainActivity

@OptIn(ExperimentalHorologistApi::class)
class TileRenderer(context: Context) :
    SingleTileLayoutRenderer<TileState, Unit>(context) {
    override fun renderTile(
        state: TileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
    ): LayoutElementBuilders.LayoutElement {
        return PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                MultiButtonLayout.Builder()
                    .apply {
                        addButton(
                            Icon.MOVE,
                            launchActivityAction(MainActivity.MODE_MOVE),
                        )
                        addButton(
                            Icon.WALK,
                            launchActivityAction(MainActivity.MODE_WALK),
                        )
                        addButton(
                            Icon.REST,
                            launchActivityAction(MainActivity.MODE_REST),
                        )
                        addButton(
                            Icon.SINGLE_LOCATION,
                            launchActivityAction(MainActivity.SINGLE_LOCATION),
                        )
                        addButton(
                            Icon.APP,
                            launchActivityAction(null),
                        )
                    }
                    .build(),
            )
            .setPrimaryChipContent(
                CompactChip.Builder(
                    context,
                    state.monitoringMode.label(context),
                    launchActivityClickable(launchActivityAction(null)),
                    deviceParameters,
                )
                    .setChipColors(ChipColors.secondaryChipColors(theme))
                    .build(),
            )
            .build()
    }

    override fun ResourceBuilders.Resources.Builder.produceRequestedResources(
        resourceState: Unit,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: List<String>,
    ) {
        for (icon in Icon.entries) {
            addIdToImageMapping(icon.id, drawableResToImageResource(icon.resId))
        }
    }

    private fun MultiButtonLayout.Builder.addButton(icon: Icon, activity: AndroidActivity) {
        val clickable = launchActivityClickable(activity)
        addButtonContent(
            Button.Builder(context, clickable)
                .setIconContent(icon.id)
                .setSize(ButtonDefaults.LARGE_SIZE)
                .setButtonColors(ButtonColors.secondaryButtonColors(theme))
                .build(),
        )
    }

    private fun launchActivityAction(action: String?): AndroidActivity = AndroidActivity.Builder()
        .setPackageName(context.packageName)
        .setClassName(ACTIVITY)
        .apply {
            if (action != null) {
                addKeyToExtraMapping(
                    MainActivity.EXTRA_KEY,
                    ActionBuilders.stringExtra(action),
                )
            }
        }
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
            "com.ramitsuri.locationtracking.wear.presentation.MainActivity"
    }
}

enum class Icon(val id: String, val resId: Int) {
    APP(
        id = "icon_app",
        resId = R.drawable.ic_open_app,
    ),
    MOVE(
        id = "icon_move",
        resId = R.drawable.ic_move,
    ),
    WALK(
        id = "icon_walk",
        resId = R.drawable.ic_walk,
    ),
    REST(
        id = "icon_rest",
        resId = R.drawable.ic_rest,
    ),
    OFF(
        id = "icon_off",
        resId = R.drawable.ic_off,
    ),
    SINGLE_LOCATION(
        id = "icon_single_location",
        resId = R.drawable.ic_single_location,
    ),
}
