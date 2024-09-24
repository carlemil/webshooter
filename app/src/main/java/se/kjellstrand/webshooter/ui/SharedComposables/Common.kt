package se.kjellstrand.webshooter.ui.SharedComposables

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

class Helpers {

    companion object {
        @Composable
        fun getStatusBarAndHeight(): Dp {
            val barPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val cutoutPadding = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
            return barPadding + cutoutPadding
        }
    }
}