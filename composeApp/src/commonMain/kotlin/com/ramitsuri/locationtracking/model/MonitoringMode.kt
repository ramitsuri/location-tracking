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
    Quiet("quiet"), // Means off
    Slow("manual"), // Named as manual in owntracks so keeping that
    Significant("significant"),
    Move("move"),
    ;

    companion object {
        fun default(): MonitoringMode {
            return Quiet
        }
    }

    fun getNextMode(): MonitoringMode {
        return when (this) {
            Quiet -> Slow

            Slow -> Significant

            Significant -> Move

            Move -> Quiet
        }
    }
}

object MonitoringModeSerializer : KSerializer<MonitoringMode> {
    override val descriptor = PrimitiveSerialDescriptor(
        "com.ramitsuri.locationtracking.model.MonitoringMode",
        PrimitiveKind.INT,
    )

    override fun serialize(encoder: Encoder, value: MonitoringMode) = when (value) {
        MonitoringMode.Quiet -> encoder.encodeInt(0)
        MonitoringMode.Slow -> encoder.encodeInt(1)
        MonitoringMode.Significant -> encoder.encodeInt(2)
        MonitoringMode.Move -> encoder.encodeInt(3)
    }

    override fun deserialize(decoder: Decoder): MonitoringMode = when (decoder.decodeInt()) {
        0 -> MonitoringMode.Quiet
        1 -> MonitoringMode.Slow
        2 -> MonitoringMode.Significant
        3 -> MonitoringMode.Move
        else -> MonitoringMode.default()
    }
}
