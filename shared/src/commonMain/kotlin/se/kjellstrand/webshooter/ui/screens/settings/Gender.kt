package se.kjellstrand.webshooter.ui.screens.settings

enum class Gender {
    UNSET,
    MALE,
    FEMALE;

    val apiValue: String? get() = when (this) {
        MALE -> "male"
        FEMALE -> "female"
        UNSET -> null
    }

    companion object {
        fun fromApiValue(value: String?): Gender = when (value) {
            "male" -> MALE
            "female" -> FEMALE
            else -> UNSET
        }
    }
}
