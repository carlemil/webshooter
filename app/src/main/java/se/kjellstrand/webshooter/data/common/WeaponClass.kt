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
    A("A"),
    B("B"),
    C("C"),
    CD("CD"),
    CJun("CJun"),
    Cvy("CVY"),
    Cvä("CVÄ"),
    R("R");

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
            else -> throw IllegalArgumentException()
        }
    }
}