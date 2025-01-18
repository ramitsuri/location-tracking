package com.ramitsuri.locationtracking.model

import com.ramitsuri.locationtracking.data.DbEnum
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MonitoringModeSerializer::class)
enum class MonitoringMode(override val value: String) : DbEnum {
    Quiet("quiet"),
    Manual("manual"),
    Significant("significant"),
    Move("move"),
}

object MonitoringModeSerializer : KSerializer<MonitoringMode> {
    override val descriptor = PrimitiveSerialDescriptor(
        "com.ramitsuri.locationtracking.model.MonitoringMode",
        PrimitiveKind.INT,
    )

    override fun serialize(encoder: Encoder, value: MonitoringMode) =
        when (value) {
            MonitoringMode.Quiet -> encoder.encodeInt(0)
            MonitoringMode.Manual -> encoder.encodeInt(1)
            MonitoringMode.Significant -> encoder.encodeInt(2)
            MonitoringMode.Move -> encoder.encodeInt(3)
        }

    override fun deserialize(decoder: Decoder): MonitoringMode =
        when (decoder.decodeInt()) {
            0 -> MonitoringMode.Quiet
            1 -> MonitoringMode.Manual
            2 -> MonitoringMode.Significant
            3 -> MonitoringMode.Move
            else -> MonitoringMode.Quiet
        }
}
