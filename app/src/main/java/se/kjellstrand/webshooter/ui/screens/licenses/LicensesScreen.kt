package se.kjellstrand.webshooter.ui.screens.licenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import se.kjellstrand.webshooter.resources.*
import se.kjellstrand.webshooter.ui.platform.UrlLauncher

data class LicenseItem(
    val name: String,
    val url: String,
    val license: String
)

@Composable
fun LicensesScreen(urlLauncher: UrlLauncher = koinInject()) {

    val licenses = listOf(
        LicenseItem("Kotlin", "https://github.com/JetBrains/kotlin", "Apache 2.0"),
        LicenseItem("Jetpack Compose", "https://developer.android.com/jetpack/compose", "Apache 2.0"),
        LicenseItem("Compose Material 3", "https://developer.android.com/jetpack/androidx/releases/compose-material3", "Apache 2.0"),
        LicenseItem("AndroidX Core KTX", "https://developer.android.com/jetpack/androidx/releases/core", "Apache 2.0"),
        LicenseItem("AndroidX Navigation Compose", "https://developer.android.com/jetpack/compose/navigation", "Apache 2.0"),
        LicenseItem("AndroidX Security Crypto", "https://developer.android.com/jetpack/androidx/releases/security", "Apache 2.0"),
        LicenseItem("AndroidX Room", "https://developer.android.com/jetpack/androidx/releases/room", "Apache 2.0"),
        LicenseItem("Hilt (Dagger)", "https://github.com/google/dagger", "Apache 2.0"),
        LicenseItem("Retrofit", "https://github.com/square/retrofit", "Apache 2.0"),
        LicenseItem("OkHttp", "https://github.com/square/okhttp", "Apache 2.0"),
        LicenseItem("Gson", "https://github.com/google/gson", "Apache 2.0"),
        LicenseItem("Firebase Crashlytics", "https://firebase.google.com/docs/crashlytics", "Apache 2.0"),
        LicenseItem("Firebase Analytics", "https://firebase.google.com/docs/analytics", "Apache 2.0"),
    )

    val apacheLicenseUrl = "https://www.apache.org/licenses/LICENSE-2.0"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.licenses_header),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.licenses_apache_license),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { urlLauncher.openUrl(apacheLicenseUrl) }
        )

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        licenses.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { urlLauncher.openUrl(item.url) }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = item.license,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
