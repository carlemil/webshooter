plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    id("com.github.triplet.play") version "3.11.0"
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.serialization)
}

val appVersionCode = 34
val appVersionName = "1.21.0"

extra["appVersionCode"] = appVersionCode
apply(from = "prebuilt-database.gradle.kts")

android {
    namespace = "se.kjellstrand.webshooter"
    compileSdk = 36

    signingConfigs {
        create("release") {
            val propsFile = rootProject.file("keystore.properties")
            val propsMap = if (propsFile.exists()) {
                propsFile.readLines()
                    .filter { it.contains('=') && !it.trimStart().startsWith('#') }
                    .associate { line ->
                        val idx = line.indexOf('=')
                        line.substring(0, idx).trim() to line.substring(idx + 1).trim()
                    }
            } else emptyMap()
            storeFile = rootProject.file(propsMap["storeFile"] ?: "keystore")
            storePassword = propsMap["storePassword"]
            keyAlias = propsMap["keyAlias"]
            keyPassword = propsMap["keyPassword"]
        }
    }

    defaultConfig {
        applicationId = "se.kjellstrand.webshooter"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "server"

    buildFeatures {
        buildConfig = true  // This enables the generation of BuildConfig class
        compose = true
    }

    productFlavors {
        val propsFile = rootProject.file("local.properties")
        val clientSecret = if (propsFile.exists()) {
            propsFile.readLines()
                .firstOrNull { it.startsWith("CLIENT_SECRET=") }
                ?.substringAfter("=")?.trim()
        } else null
        val secretField = "\"${clientSecret ?: "REMOVED-CLIENT-SECRET"}\""

        create("prod") {
            dimension = "server"
            buildConfigField("String", "BASE_URL", "\"https://webshooter.se/\"")
            buildConfigField("String", "CLIENT_SECRET", secretField)
        }
        create("staging") {
            dimension = "server"
            applicationIdSuffix = ".staging"
            buildConfigField("String", "BASE_URL", "\"https://staging.webshooter.se/\"")
            buildConfigField("String", "CLIENT_SECRET", secretField)
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

}

play {
    serviceAccountCredentials.set(rootProject.file("play-account.json"))
    track.set("internal")
    defaultToAppBundles.set(true)
}

dependencies {

    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // androidx-security-crypto comes via :shared/androidMain (used by AuthTokenManager + SecurePrefs).
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation comes via :shared commonMain (JB multiplatform port).

    // Koin (DI) — koin-core comes via :shared as api dep
    implementation(libs.koin.compose.viewmodel)

    // Networking
    implementation(libs.okhttp)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Datetime
    implementation(libs.kotlinx.datetime)

    // Logging (multiplatform)
    implementation(libs.napier)

    // Crypto (multiplatform SHA-1)
    implementation(libs.kotlincrypto.hash.sha1)

    // multiplatform-settings comes via :shared/commonMain.

    // Room (runtime comes via :shared; only the Android-specific ktx
    //  helpers stay here for Room.databaseBuilder in DatabaseModule).
    implementation(libs.androidx.room.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Charts: pure Compose Canvas — see ChartStyles.kt + chart screens.

    // Extended Icons
    implementation(libs.androidx.material.icons.extended)

    // Test
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockwebserver)

}
