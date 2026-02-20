package se.kjellstrand.webshooter.data.common

import androidx.annotation.StringRes
import se.kjellstrand.webshooter.R

enum class CompetitionStatus(val status: String, @StringRes val labelRes: Int) {
    OPEN("open", R.string.competition_status_open),
    ALL("all", R.string.competition_status_all),
    COMPLETED("completed", R.string.competition_status_completed),
    UPCOMING("upcoming", R.string.competition_status_upcoming),
    CLOSED("closed", R.string.competition_status_closed)
}