package se.kjellstrand.webshooter.data.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class WeaponClass(
    val id: Long,

    val classname: String,

    @SerialName("classname_general")
    val classnameGeneral: ClassnameGeneral
)

@Serializable(with = ClassnameGeneralSerializer::class)
enum class ClassnameGeneral(val value: String) {
    A("A"),
    AOpt("A Opt"),
    AG("AG"),
    AM("AM"),
    AP("AP"),
    B("B"),
    C("C"),
    CD("CD"),
    CJun("CJun"),
    Cvy("CVY"),
    Cvä("CVÄ"),
    R("R"),
    M1("M1"),
    M2("M2"),
    M3("M3"),
    M4("M4"),
    M5("M5"),
    M6("M6"),
    M7("M7"),
    M8("M8"),
    M9("M9"),
    OptR("OptR"),

    /** Fallback for any value the backend introduces that this client doesn't know yet. */
    Unknown("__unknown__")
}

object ClassnameGeneralSerializer : KSerializer<ClassnameGeneral> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ClassnameGeneral", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ClassnameGeneral) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): ClassnameGeneral {
        val raw = decoder.decodeString()
        return ClassnameGeneral.values().firstOrNull { it.value == raw } ?: ClassnameGeneral.Unknown
    }
}
