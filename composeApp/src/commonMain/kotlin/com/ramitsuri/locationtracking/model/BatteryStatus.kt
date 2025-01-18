package com.ramitsuri.locationtracking.model

import com.ramitsuri.locationtracking.data.DbEnum
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = BatteryStatusSerializer::class)
enum class BatteryStatus(override val value: String) : DbEnum {
    UNKNOWN("unknown"),
    UNPLUGGED("unplugged"),
    CHARGING("charging"),
    FULL("full"),
}

object BatteryStatusSerializer : KSerializer<BatteryStatus> {
    override val descriptor = PrimitiveSerialDescriptor(
        "com.ramitsuri.locationtracking.model.BatteryStatus",
        PrimitiveKind.INT,
    )

    override fun serialize(encoder: Encoder, value: BatteryStatus) = when (value) {
        BatteryStatus.UNKNOWN -> encoder.encodeInt(0)
        BatteryStatus.UNPLUGGED -> encoder.encodeInt(1)
        BatteryStatus.CHARGING -> encoder.encodeInt(2)
        BatteryStatus.FULL -> encoder.encodeInt(3)
    }

    override fun deserialize(decoder: Decoder): BatteryStatus = when (decoder.decodeInt()) {
        0 -> BatteryStatus.UNKNOWN
        1 -> BatteryStatus.UNPLUGGED
        2 -> BatteryStatus.CHARGING
        3 -> BatteryStatus.FULL
        else -> BatteryStatus.UNKNOWN
    }
}
