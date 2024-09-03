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

    flavorDimensions += "server"

    productFlavors {
        create("mock") {
            dimension = "server"
            buildConfigField("String", "BASE_URL", "\"http://localhost:8080/\"")
        }
        create("prod") {
            dimension = "server"
            buildConfigField("String", "BASE_URL", "\"https://your.real.backend.url/\"")
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
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    kapt(libs.androidx.room.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockito.core)
    testImplementation(libs.byte.buddy)
    testImplementation(libs.okhttp3.mockwebserver)
    androidTestImplementation(libs.okhttp3.mockwebserver)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.core.testing)
    androidTestImplementation(libs.truth)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}
