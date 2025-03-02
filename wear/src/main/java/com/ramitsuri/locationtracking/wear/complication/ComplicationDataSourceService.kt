package com.ramitsuri.locationtracking.wear.complication

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.ramitsuri.locationtracking.R
import com.ramitsuri.locationtracking.model.MonitoringMode
import com.ramitsuri.locationtracking.settings.Settings
import com.ramitsuri.locationtracking.ui.label
import com.ramitsuri.locationtracking.wear.presentation.MainActivity
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ComplicationDataSourceService : SuspendingComplicationDataSourceService(), KoinComponent {
    private val settings: Settings by inject()
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        return getComplicationData(settings.getMonitoringMode().first())
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData = getComplicationData()

    private fun getComplicationData(
        mode: MonitoringMode = MonitoringMode.default(),
    ): ComplicationData = when (mode) {
        MonitoringMode.Off -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                Icon.createWithResource(this, R.drawable.ic_off),
            ).build(),
            contentDescription = PlainComplicationText.Builder(
                text = mode.label(applicationContext),
            ).build(),
        )

        MonitoringMode.Rest -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                Icon.createWithResource(this, R.drawable.ic_rest),
            ).build(),
            contentDescription = PlainComplicationText.Builder(
                text = mode.label(applicationContext),
            ).build(),
        )

        MonitoringMode.Walk -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                Icon.createWithResource(this, R.drawable.ic_walk),
            ).build(),
            contentDescription = PlainComplicationText.Builder(
                text = mode.label(applicationContext),
            ).build(),
        )

        MonitoringMode.Move -> MonochromaticImageComplicationData.Builder(
            monochromaticImage = MonochromaticImage.Builder(
                Icon.createWithResource(this, R.drawable.ic_move),
            ).build(),
            contentDescription = PlainComplicationText.Builder(
                text = mode.label(applicationContext),
            ).build(),
        )
    }.setTapAction(
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        ),
    ).build()

    companion object {
        fun update(context: Context) {
            ComplicationDataSourceUpdateRequester.create(
                context,
                ComponentName(
                    context,
                    ComplicationDataSourceService::class.java,
                ),
            ).requestUpdateAll()
        }
    }
}
