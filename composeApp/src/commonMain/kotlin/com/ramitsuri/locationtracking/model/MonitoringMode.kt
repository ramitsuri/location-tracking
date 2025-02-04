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
    val horizontalAccuracyMeters: Float = 0f,
) : DbEnum {
    // Owntracks quiet
    Off(
        value = "off",
    ),

    // Owntracks manual - very slow updates
    Rest(
        value = "rest",
        horizontalAccuracyMeters = 200f,
    ),

    Walk(
        value = "walk",
        horizontalAccuracyMeters = 50f,
    ),

    Move(
        value = "move",
        horizontalAccuracyMeters = 100f,
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

            Rest -> Walk

            Walk -> Move

            Move -> Rest
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
        MonitoringMode.Walk -> encoder.encodeInt(2)
        MonitoringMode.Move -> encoder.encodeInt(3)
    }

    override fun deserialize(decoder: Decoder): MonitoringMode = when (decoder.decodeInt()) {
        0 -> MonitoringMode.Off
        1 -> MonitoringMode.Rest
        2 -> MonitoringMode.Walk
        3 -> MonitoringMode.Move
        else -> MonitoringMode.default()
    }
}
