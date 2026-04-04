package se.kjellstrand.webshooter.ui.navigation

import android.os.Bundle

object NavigationArguments {

    fun requireLong(arguments: Bundle?, key: String): Long {
        if (arguments == null) {
            throw IllegalArgumentException("Navigation arguments bundle is null, missing required argument: $key")
        }
        if (!arguments.containsKey(key)) {
            throw IllegalArgumentException("Missing required navigation argument: $key")
        }
        return arguments.getLong(key)
    }

    fun requireInt(arguments: Bundle?, key: String): Int {
        if (arguments == null) {
            throw IllegalArgumentException("Navigation arguments bundle is null, missing required argument: $key")
        }
        if (!arguments.containsKey(key)) {
            throw IllegalArgumentException("Missing required navigation argument: $key")
        }
        return arguments.getInt(key)
    }

    fun requireString(arguments: Bundle?, key: String): String {
        if (arguments == null) {
            throw IllegalArgumentException("Navigation arguments bundle is null, missing required argument: $key")
        }
        return arguments.getString(key)
            ?: throw IllegalArgumentException("Missing required navigation argument: $key")
    }
}
