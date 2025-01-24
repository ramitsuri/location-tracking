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
    Off("off"), // Owntracks quiet
    Slow("slow"), // Owntracks manual
    SignificantChanges("significant"),
    Moving("move"),
    ;

    companion object {
        fun default(): MonitoringMode {
            return Off
        }
    }

    fun getNextMode(): MonitoringMode {
        return when (this) {
            Off -> Slow

            Slow -> SignificantChanges

            SignificantChanges -> Moving

            Moving -> Off
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
        MonitoringMode.Slow -> encoder.encodeInt(1)
        MonitoringMode.SignificantChanges -> encoder.encodeInt(2)
        MonitoringMode.Moving -> encoder.encodeInt(3)
    }

    override fun deserialize(decoder: Decoder): MonitoringMode = when (decoder.decodeInt()) {
        0 -> MonitoringMode.Off
        1 -> MonitoringMode.Slow
        2 -> MonitoringMode.SignificantChanges
        3 -> MonitoringMode.Moving
        else -> MonitoringMode.default()
    }
}
