package se.kjellstrand.webshooter.data.clubstats

enum class WeaponClassGroup(val prefix: String) {
    A("A"), B("B"), C("C"), R("R"),
    M1("M1"), M2("M2"), M3("M3"), M4("M4"), M5("M5"),
    M6("M6"), M7("M7"), M8("M8"), M9("M9");

    fun matches(weaponClass: String): Boolean =
        weaponClass.isNotEmpty() && weaponClass.startsWith(prefix, ignoreCase = true)
}
