package se.kjellstrand.webshooter.data.common

import androidx.annotation.StringRes
import se.kjellstrand.webshooter.R

enum class CompetitionStatus(val status: String, @StringRes val labelRes: Int) {
    MY_ENTRIES("all", R.string.my_entries),
    ALL("all", R.string.competition_status_all),
    OPEN("open", R.string.competition_status_open),
    COMPLETED("completed", R.string.competition_status_completed),
    UPCOMING("upcoming", R.string.competition_status_upcoming),
    CLOSED("closed", R.string.competition_status_closed)
}