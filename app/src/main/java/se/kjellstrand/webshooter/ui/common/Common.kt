package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.max

class Common {

    companion object {
        @Composable
        fun getStatusBarOrCutOutHeight(): Dp {
            val barPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val cutoutPadding = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
            return max(barPadding, cutoutPadding)
        }
    }
}