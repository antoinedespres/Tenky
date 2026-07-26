package fr.dutapp.tenky.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * `java.time` serializers for the cached snapshot.
 *
 * Values are stored numerically rather than as formatted text so they cannot be
 * misread under a different locale when the cache is loaded back.
 */

internal object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: LocalTime) =
        encoder.encodeInt(value.toSecondOfDay())

    override fun deserialize(decoder: Decoder): LocalTime =
        LocalTime.ofSecondOfDay(decoder.decodeInt().toLong())
}

internal object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalDate) =
        encoder.encodeLong(value.toEpochDay())

    override fun deserialize(decoder: Decoder): LocalDate =
        LocalDate.ofEpochDay(decoder.decodeLong())
}

internal object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)

    // Stored against UTC purely as an encoding: the value is already local to
    // the place being shown, and no offset is reapplied on the way back.
    override fun serialize(encoder: Encoder, value: LocalDateTime) =
        encoder.encodeLong(value.toEpochSecond(ZoneOffset.UTC))

    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.ofEpochSecond(decoder.decodeLong(), 0, ZoneOffset.UTC)
}

internal object ZoneOffsetSerializer : KSerializer<ZoneOffset> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ZoneOffset", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ZoneOffset) =
        encoder.encodeInt(value.totalSeconds)

    override fun deserialize(decoder: Decoder): ZoneOffset =
        ZoneOffset.ofTotalSeconds(decoder.decodeInt())
}
