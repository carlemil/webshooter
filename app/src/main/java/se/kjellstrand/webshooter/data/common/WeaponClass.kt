package se.kjellstrand.webshooter.data.common

import com.google.gson.annotations.SerializedName
import se.kjellstrand.webshooter.data.competitions.remote.Pivot

data class WeaponClass(
    val id: Long,

    @SerializedName("weapongroups_id")
    val weaponGroupsID: Long,

    val classname: String,
    val championship: Long,

    @SerializedName("classname_general")
    val classnameGeneral: ClassnameGeneral,

    val pivot: Pivot?
)

enum class ClassnameGeneral(val value: String) {
    @SerializedName("A") A("A"),
    @SerializedName("B") B("B"),
    @SerializedName("C") C("C"),
    @SerializedName("CD") CD("CD"),
    @SerializedName("CJun") CJun("CJun"),
    @SerializedName("CVY") Cvy("CVY"),
    @SerializedName("CVÄ") Cvä("CVÄ"),
    @SerializedName("R") R("R"),
    @SerializedName("M1") M1("M1"),
    @SerializedName("M2") M2("M2"),
    @SerializedName("M3") M3("M3"),
    @SerializedName("M4") M4("M4"),
    @SerializedName("M5") M5("M5"),
    @SerializedName("M6") M6("M6"),
    @SerializedName("M7") M7("M7"),
    @SerializedName("M8") M8("M8"),
    @SerializedName("M9") M9("M9"),
    @SerializedName("OptR") OptR("OptR");

    companion object {
        public fun fromValue(value: String): ClassnameGeneral = when (value) {
            "A" -> A
            "B" -> B
            "C" -> C
            "CD" -> CD
            "CJun" -> CJun
            "CVY" -> Cvy
            "CVÄ", "CV\u00c4" -> Cvä
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
