package se.kjellstrand.webshooter.data.competitions.remote.parsing


enum class ClassnameGeneral(val value: String) {
    A("A"),
    B("B"),
    C("C"),
    CD("CD"),
    CJun("CJun"),
    Cvy("CVY"),
    Cvä("CVÄ"),
    M1("M1"),
    M2("M2"),
    R("R");

    companion object {
        public fun fromValue(value: String): ClassnameGeneral = when (value) {
            "A"    -> A
            "B"    -> B
            "C"    -> C
            "CD"   -> CD
            "CJun" -> CJun
            "CVY"  -> Cvy
            "CVÄ"  -> Cvä
            "M1"   -> M1
            "M2"   -> M2
            "R"    -> R
            else   -> throw IllegalArgumentException()
        }
    }
}
