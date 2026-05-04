package se.kjellstrand.webshooter.ui.screens.settings

import androidx.annotation.StringRes
import se.kjellstrand.webshooter.R

enum class Gender(@StringRes val labelRes: Int) {
    UNSET(R.string.select_gender),
    MALE(R.string.male),
    FEMALE(R.string.female);

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
