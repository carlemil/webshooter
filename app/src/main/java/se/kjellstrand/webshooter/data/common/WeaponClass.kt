package se.kjellstrand.webshooter.data.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeaponClass(
    val id: Long,

    val classname: String,

    @SerialName("classname_general")
    val classnameGeneral: ClassnameGeneral
)

@Serializable
enum class ClassnameGeneral(val value: String) {
    @SerialName("A") A("A"),
    @SerialName("B") B("B"),
    @SerialName("C") C("C"),
    @SerialName("CD") CD("CD"),
    @SerialName("CJun") CJun("CJun"),
    @SerialName("CVY") Cvy("CVY"),
    @SerialName("CVÄ") Cvä("CVÄ"),
    @SerialName("R") R("R"),
    @SerialName("M1") M1("M1"),
    @SerialName("M2") M2("M2"),
    @SerialName("M3") M3("M3"),
    @SerialName("M4") M4("M4"),
    @SerialName("M5") M5("M5"),
    @SerialName("M6") M6("M6"),
    @SerialName("M7") M7("M7"),
    @SerialName("M8") M8("M8"),
    @SerialName("M9") M9("M9"),
    @SerialName("OptR") OptR("OptR");

    companion object {
        public fun fromValue(value: String): ClassnameGeneral = when (value) {
            "A" -> A
            "B" -> B
            "C" -> C
            "CD" -> CD
            "CJun" -> CJun
            "CVY" -> Cvy
            "CVÄ" -> Cvä
            "R" -> R
            "M1" -> M1
            "M2" -> M2
            "M3" -> M3
            "M4" -> M4
            "M5" -> M5
            "M6" -> M6
            "M7" -> M7
            "M8" -> M8
            "M9" -> M9
            "OptR" -> OptR
            else -> throw IllegalArgumentException("Unknown ClassnameGeneral value: $value")
        }
    }
}
