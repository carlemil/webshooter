package se.kjellstrand.webshooter.ui.common

import se.kjellstrand.webshooter.data.results.remote.Result

data class GroupedItem(
    val header: String,
    val items: List<Result>
)
