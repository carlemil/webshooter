plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    id("com.github.triplet.play") version "3.11.0"
}

val appVersionCode = 18
val appVersionName = "1.9.0"

// ---- Generate release notes ----
// Make release notes for everything in between appVersionName 1.8.0 and appVersionName 1.9.0.
// Focus on what the USER experiences, not what the dev team did. Max 500 characters.
// Short, user-friendly language. No technical jargon.

val dbVersionDir = layout.buildDirectory.dir("generated/source/dbversion")

val generateDbVersion = tasks.register("generateDbVersion") {
    outputs.dir(dbVersionDir)
    doLast {
        val dir = dbVersionDir.get().asFile
        dir.mkdirs()
        File(dir, "DbVersion.kt").writeText(
            "package se.kjellstrand.webshooter.data.db\n\nconst val DB_VERSION = $appVersionCode\n"
        )
    }
}

tasks.configureEach {
    if (name.startsWith("ksp") || (name.startsWith("compile") && name.endsWith("Kotlin"))) {
        dependsOn(generateDbVersion)
    }
}

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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    sourceSets {
        getByName("main").java.srcDir(dbVersionDir)
    }

    flavorDimensions += "server"

    buildFeatures {
        buildConfig = true  // This enables the generation of BuildConfig class
        compose = true
    }

    productFlavors {
        create("prod") {
            dimension = "server"
            buildConfigField("String", "BASE_URL", "\"https://webshooter.se/\"")
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

}

play {
    serviceAccountCredentials.set(rootProject.file("play-account.json"))
    track.set("internal")
    defaultToAppBundles.set(true)
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.security.crypto.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Dagger - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Extended Icons
    implementation(libs.androidx.material.icons.extended)

    // Test
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockwebserver)

}
