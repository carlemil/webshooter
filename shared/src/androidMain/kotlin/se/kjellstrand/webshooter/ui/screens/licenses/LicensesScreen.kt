@file:JvmName("LicensesScreenAndroid")

package se.kjellstrand.webshooter.ui.screens.licenses

import androidx.compose.runtime.Composable
import kotlin.jvm.JvmName

private val AndroidLicenses = listOf(
    LicenseItem("Kotlin", "https://github.com/JetBrains/kotlin", "Apache 2.0"),
    LicenseItem("Kotlinx Coroutines", "https://github.com/Kotlin/kotlinx.coroutines", "Apache 2.0"),
    LicenseItem("Kotlinx Serialization", "https://github.com/Kotlin/kotlinx.serialization", "Apache 2.0"),
    LicenseItem("Kotlinx DateTime", "https://github.com/Kotlin/kotlinx-datetime", "Apache 2.0"),
    LicenseItem("Compose Multiplatform", "https://github.com/JetBrains/compose-multiplatform", "Apache 2.0"),
    LicenseItem("Jetpack Compose", "https://developer.android.com/jetpack/compose", "Apache 2.0"),
    LicenseItem("Compose Material 3", "https://developer.android.com/jetpack/androidx/releases/compose-material3", "Apache 2.0"),
    LicenseItem("Material Icons Extended", "https://developer.android.com/jetpack/androidx/releases/compose-material", "Apache 2.0"),
    LicenseItem("AndroidX Activity Compose", "https://developer.android.com/jetpack/androidx/releases/activity", "Apache 2.0"),
    LicenseItem("AndroidX Core KTX", "https://developer.android.com/jetpack/androidx/releases/core", "Apache 2.0"),
    LicenseItem("AndroidX Lifecycle ViewModel", "https://developer.android.com/jetpack/androidx/releases/lifecycle", "Apache 2.0"),
    LicenseItem("Navigation Compose (JetBrains Multiplatform)", "https://github.com/JetBrains/compose-multiplatform-core", "Apache 2.0"),
    LicenseItem("AndroidX Room", "https://developer.android.com/jetpack/androidx/releases/room", "Apache 2.0"),
    LicenseItem("AndroidX SQLite Bundled", "https://developer.android.com/jetpack/androidx/releases/sqlite", "Apache 2.0"),
    LicenseItem("AndroidX Security Crypto", "https://developer.android.com/jetpack/androidx/releases/security", "Apache 2.0"),
    LicenseItem("Koin", "https://github.com/InsertKoinIO/koin", "Apache 2.0"),
    LicenseItem("Ktor", "https://github.com/ktorio/ktor", "Apache 2.0"),
    LicenseItem("OkHttp", "https://github.com/square/okhttp", "Apache 2.0"),
    LicenseItem("Okio", "https://github.com/square/okio", "Apache 2.0"),
    LicenseItem("Napier", "https://github.com/AAkira/Napier", "Apache 2.0"),
    LicenseItem("Multiplatform Settings", "https://github.com/russhwolf/multiplatform-settings", "Apache 2.0"),
    LicenseItem("KotlinCrypto Hash SHA1", "https://github.com/KotlinCrypto/hash", "Apache 2.0"),
    LicenseItem("Accompanist System UI Controller", "https://github.com/google/accompanist", "Apache 2.0"),
    LicenseItem("Firebase Crashlytics", "https://firebase.google.com/docs/crashlytics", "Apache 2.0"),
    LicenseItem("Firebase Analytics", "https://firebase.google.com/docs/analytics", "Apache 2.0"),
)

@Composable
actual fun LicensesScreen() {
    LicensesContent(AndroidLicenses)
}
