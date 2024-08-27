plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    //id("com.google.dagger.hilt.android")
    kotlin("kapt") version "1.9.0"
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "se.kjellstrand.data"
    compileSdk = 34

    defaultConfig {
        applicationId = "se.kjellstrand.data"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    kapt("androidx.room:room-compiler:2.6.1")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.core.testing)
    testImplementation(libs.truth)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    testImplementation(libs.mockito.core)
    testImplementation(libs.byte.buddy)
    // If using Android instrumentation tests
    androidTestImplementation(libs.mockito.core)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}
