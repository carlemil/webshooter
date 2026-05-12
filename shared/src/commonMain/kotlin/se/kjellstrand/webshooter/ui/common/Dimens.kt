package se.kjellstrand.webshooter.ui.common

import androidx.compose.ui.unit.dp

/**
 * Static UI constants that previously lived in `app/src/main/res/values/dimens.xml`
 * and were read via `dimensionResource(R.dimen.X)` / `integerResource(R.integer.X)`.
 *
 * Baked here as Kotlin so they survive the move into commonMain — Compose
 * Resources doesn't have a Dp/Int resource type, so there's no `Res.dimen.X`
 * equivalent. Two constants total, low maintenance.
 */
object Dimens {
    val ScreenContentTopPadding = 8.dp
    const val RoundedCornerShapePercent: Int = 10
}
