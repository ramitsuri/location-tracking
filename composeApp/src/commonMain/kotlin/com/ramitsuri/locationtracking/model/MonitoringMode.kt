package com.ramitsuri.locationtracking.model

import com.ramitsuri.locationtracking.data.DbEnum
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MonitoringModeSerializer::class)
enum class MonitoringMode(
    override val value: String,
) : DbEnum {
    // Owntracks quiet
    Off(
        value = "off",
    ),

    // Owntracks manual - very slow updates
    Rest(
        value = "rest",
    ),

    Move(
        value = "move",
    ),

    TempRegion(
        value = "temp_region",
    ),
    ;

    companion object {
        fun default(): MonitoringMode {
            return Off
        }
    }

    fun getNextMode(): MonitoringMode {
        return when (this) {
            Off -> Rest

            Rest -> Move

            Move -> Rest

            TempRegion -> Move
        }
    }
}

object MonitoringModeSerializer : KSerializer<MonitoringMode> {
    override val descriptor = PrimitiveSerialDescriptor(
        "com.ramitsuri.locationtracking.model.MonitoringMode",
        PrimitiveKind.INT,
    )

    override fun serialize(encoder: Encoder, value: MonitoringMode) = when (value) {
        MonitoringMode.Off -> encoder.encodeInt(0)
        MonitoringMode.Rest -> encoder.encodeInt(1)
        MonitoringMode.Move -> encoder.encodeInt(3)
        MonitoringMode.TempRegion -> encoder.encodeInt(4)
    }

    override fun deserialize(decoder: Decoder): MonitoringMode = when (decoder.decodeInt()) {
        0 -> MonitoringMode.Off
        1 -> MonitoringMode.Rest
        3 -> MonitoringMode.Move
        4 -> MonitoringMode.TempRegion
        else -> MonitoringMode.default()
    }
}
