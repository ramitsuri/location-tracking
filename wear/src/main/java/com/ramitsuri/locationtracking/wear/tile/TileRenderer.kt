package com.ramitsuri.locationtracking.wear.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ActionBuilders.AndroidActivity
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textButton
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.padding
import androidx.wear.protolayout.types.layoutString
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.ui.label
import com.ramitsuri.locationtracking.wear.presentation.MainActivity

fun tileLayout(
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters,
    state: TileState,
): LayoutElement {
    return materialScope(
        context = context,
        deviceConfiguration = deviceParameters,
        allowDynamicTheme = true,
    ) {
        primaryLayout(
            mainSlot = {
                column {
                    setWidth(expand())
                    setHeight(expand())
                    addContent(
                        buttonGroup {
                            buttonGroupItem {
                                button(
                                    action = launchActivityAction(context, MainActivity.MODE_MOVE),
                                    text = MonitoringMode.Move.label(context),
                                )
                            }
                            buttonGroupItem {
                                button(
                                    action = launchActivityAction(context, MainActivity.MODE_WALK),
                                    text = MonitoringMode.Walk.label(context),
                                )
                            }
                        },
                    )
                    addContent(ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
                    addContent(
                        buttonGroup {
                            buttonGroupItem {
                                button(
                                    action = launchActivityAction(context, MainActivity.MODE_REST),
                                    text = MonitoringMode.Rest.label(context),
                                )
                            }
                            buttonGroupItem {
                                button(
                                    action = launchActivityAction(
                                        context,
                                        MainActivity.SINGLE_LOCATION,
                                    ),
                                    text = context.getString(R.string.once),
                                )
                            }
                        },
                    )
                }
            },
            bottomSlot = {
                textEdgeButton(
                    onClick = launchActivityClickable(launchActivityAction(context)),
                    labelContent = { text(state.monitoringMode.label(context).layoutString) },
                    colors = filledTonalButtonColors(),
                )
            },
        )
    }
}

private fun MaterialScope.button(action: AndroidActivity, text: String): LayoutElement {
    return textButton(
        labelContent = { text(text.layoutString) },
        height = expand(),
        width = expand(),
        onClick = launchActivityClickable(action),
        contentPadding = padding(0f),
    )
}

private fun launchActivityAction(context: Context, action: String? = null): AndroidActivity =
    AndroidActivity.Builder()
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

private fun column(builder: LayoutElementBuilders.Column.Builder.() -> Unit) =
    LayoutElementBuilders.Column.Builder().apply(builder).build()

private const val ACTIVITY =
    "com.ramitsuri.locationtracking.wear.presentation.MainActivity"
