import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    // Make the generated Res accessor public + put it in a sensible
    // package so :app (and the future iOS host) can reference
    // se.kjellstrand.webshooter.resources.Res.string.foo without imports
    // from auto-sanitized package names like `web_shooter.shared.*`.
    publicResClass = true
    packageOfResClass = "se.kjellstrand.webshooter.resources"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Public API surface — exposed transitively to :app.
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.napier)
            api(libs.androidx.room.runtime)

            // Internal helpers used by shared classes.
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.okio)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.kotlincrypto.hash.sha1)
            api(libs.multiplatform.settings)
            api(libs.koin.core)
            api(libs.koin.core.viewmodel)
            api(libs.koin.compose.viewmodel)
            api(libs.androidx.lifecycle.viewmodel)

            // Compose Multiplatform (used by screens being moved into commonMain).
            // The JetBrains Compose artifacts re-export the same FQNs as
            // AndroidX Compose, so consumer code doesn't need import changes.
            // api(...) so :app can use the same Compose Multiplatform
            // artifacts and the generated Res class transitively.
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.materialIconsExtended)
            api(compose.components.resources)
            api(compose.components.uiToolingPreview)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.security.crypto)
            implementation(libs.androidx.security.crypto.ktx)
            // OkHttp engine for the Android HttpClient factory; the
            // engine block also needs okhttp3.Interceptor on the classpath
            // so :app can pass MockInterceptor through extraOkHttpInterceptors.
            api(libs.ktor.client.okhttp)
            api(libs.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "se.kjellstrand.webshooter.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
