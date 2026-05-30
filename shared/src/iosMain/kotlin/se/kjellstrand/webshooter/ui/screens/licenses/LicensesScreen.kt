package se.kjellstrand.webshooter.ui.screens.licenses

import androidx.compose.runtime.Composable

private val IosLicenses = listOf(
    LicenseItem("Kotlin", "https://github.com/JetBrains/kotlin", "Apache 2.0"),
    LicenseItem("Kotlin/Native", "https://github.com/JetBrains/kotlin/tree/master/kotlin-native", "Apache 2.0"),
    LicenseItem("Kotlinx Coroutines", "https://github.com/Kotlin/kotlinx.coroutines", "Apache 2.0"),
    LicenseItem("Kotlinx Serialization", "https://github.com/Kotlin/kotlinx.serialization", "Apache 2.0"),
    LicenseItem("Kotlinx DateTime", "https://github.com/Kotlin/kotlinx-datetime", "Apache 2.0"),
    LicenseItem("Compose Multiplatform", "https://github.com/JetBrains/compose-multiplatform", "Apache 2.0"),
    LicenseItem("Compose Material 3", "https://developer.android.com/jetpack/androidx/releases/compose-material3", "Apache 2.0"),
    LicenseItem("Navigation Compose (JetBrains Multiplatform)", "https://github.com/JetBrains/compose-multiplatform-core", "Apache 2.0"),
    LicenseItem("AndroidX Lifecycle ViewModel (KMP)", "https://developer.android.com/jetpack/androidx/releases/lifecycle", "Apache 2.0"),
    LicenseItem("Room (KMP)", "https://developer.android.com/jetpack/androidx/releases/room", "Apache 2.0"),
    LicenseItem("SQLite (KMP)", "https://developer.android.com/jetpack/androidx/releases/sqlite", "Apache 2.0"),
    LicenseItem("Koin", "https://github.com/InsertKoinIO/koin", "Apache 2.0"),
    LicenseItem("Ktor (Darwin engine)", "https://github.com/ktorio/ktor", "Apache 2.0"),
    LicenseItem("Okio", "https://github.com/square/okio", "Apache 2.0"),
    LicenseItem("Napier", "https://github.com/AAkira/Napier", "Apache 2.0"),
    LicenseItem("Multiplatform Settings (Keychain)", "https://github.com/russhwolf/multiplatform-settings", "Apache 2.0"),
    LicenseItem("KotlinCrypto Hash SHA1", "https://github.com/KotlinCrypto/hash", "Apache 2.0"),
)

@Composable
actual fun LicensesScreen() {
    LicensesContent(IosLicenses)
}
