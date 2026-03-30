package se.kjellstrand.webshooter.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

/**
 * Only navigate if the current back stack entry is resumed,
 * preventing duplicate navigation from rapid clicks.
 */
fun NavController.safePopBackStack(): Boolean {
    return if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    } else {
        false
    }
}

fun NavController.safeNavigate(route: String, builder: (androidx.navigation.NavOptionsBuilder.() -> Unit)? = null) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        if (builder != null) {
            navigate(route, builder)
        } else {
            navigate(route)
        }
    }
}
