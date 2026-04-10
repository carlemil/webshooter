import java.io.IOException
import java.sql.DriverManager
import java.util.Properties
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    id("com.github.triplet.play") version "3.11.0"
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val appVersionCode = 29
val appVersionName = "1.17.0"

val dbVersionDir = layout.buildDirectory.dir("generated/source/dbversion")

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

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

val generatePrebuiltDatabase = tasks.register("generatePrebuiltDatabase") {
    description = "Fetches competition results from the API and populates a prebuilt Room database asset"

    val schemaDir = file("schemas/se.kjellstrand.webshooter.data.db.AppDatabase")
    val dbFile = file("src/main/assets/databases/webshooter.db")
    val maxCompetitionId = 300

    inputs.dir(schemaDir)
    outputs.file(dbFile)

    doLast {
        val schemaFile = File(schemaDir, "$appVersionCode.json")
        if (!schemaFile.exists()) {
            throw GradleException(
                "Room schema not found at ${schemaFile.path}. Run :app:kspProdReleaseKotlin first."
            )
        }

        // Read credentials from local.properties
        val propsFile = rootProject.file("local.properties")
        val props = Properties()
        if (propsFile.exists()) {
            propsFile.inputStream().use { props.load(it) }
        }
        val email = props.getProperty("PREBUILD_EMAIL")
            ?: throw GradleException("PREBUILD_EMAIL not set in local.properties")
        val password = props.getProperty("PREBUILD_PASSWORD")
            ?: throw GradleException("PREBUILD_PASSWORD not set in local.properties")
        val clientSecret = props.getProperty("CLIENT_SECRET")
            ?: "52FphTYzOrmuqH30ltL7LrBzhSEURIJiMFNp6Qt0"

        val schemaJson = com.google.gson.JsonParser.parseString(schemaFile.readText()).asJsonObject
        val database = schemaJson.getAsJsonObject("database")
        val identityHash = database.get("identityHash").asString
        val entities = database.getAsJsonArray("entities")

        dbFile.parentFile.mkdirs()

        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val gson = com.google.gson.Gson()

        // Authenticate via OAuth
        val loginBody = okhttp3.RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            gson.toJson(mapOf(
                "client_id" to 1,
                "client_secret" to clientSecret,
                "email" to email,
                "grant_type" to "password",
                "password" to password,
                "username" to email
            ))
        )
        val loginRequest = okhttp3.Request.Builder()
            .url("https://webshooter.se/api/v4.1.9/oauth/token")
            .post(loginBody)
            .header("Accept", "application/json")
            .build()
        val loginResponse = client.newCall(loginRequest).execute()
        if (!loginResponse.isSuccessful) {
            throw GradleException("Login failed with HTTP ${loginResponse.code}: ${loginResponse.body?.string()}")
        }
        val loginJson = com.google.gson.JsonParser.parseString(loginResponse.body!!.string()).asJsonObject
        val accessToken = loginJson.get("access_token").asString
        logger.lifecycle("Authenticated successfully")

        DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}").use { conn ->
            conn.autoCommit = false

            // Create room_master_table
            conn.createStatement().use { stmt ->
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)"
                )
                stmt.execute(
                    "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '$identityHash')"
                )
            }

            // Create all entity tables from schema
            for (entity in entities) {
                val entityObj = entity.asJsonObject
                val tableName = entityObj.get("tableName").asString
                val createSql = entityObj.get("createSql").asString
                    .replace("\${TABLE_NAME}", tableName)
                conn.createStatement().use { stmt ->
                    stmt.execute(createSql)
                }
                // Create indices
                val indices = entityObj.getAsJsonArray("indices")
                for (index in indices) {
                    val indexSql = index.asJsonObject.get("createSql").asString
                        .replace("\${TABLE_NAME}", tableName)
                    conn.createStatement().use { stmt ->
                        stmt.execute(indexSql)
                    }
                }
            }

            // Create competition_fetch_status tracking table
            conn.createStatement().use { stmt ->
                stmt.execute(
                    """CREATE TABLE IF NOT EXISTS competition_fetch_status (
                        competitionId INTEGER PRIMARY KEY,
                        status TEXT NOT NULL
                    )"""
                )
            }

            conn.commit()

            // Check which competitions need fetching
            val skipIds = mutableSetOf<Long>()
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT competitionId, status FROM competition_fetch_status")
                while (rs.next()) {
                    val id = rs.getLong("competitionId")
                    val status = rs.getString("status")
                    when (status) {
                        "has_results", "no_results", "invalid" -> skipIds.add(id)
                    }
                }
            }

            val insertResult = conn.prepareStatement(
                """INSERT OR REPLACE INTO results
                    (id, competitionsId, signupsId, placement, figureHits, hits, points, stdMedal,
                     signupJson, weaponClassJson, stationResultsJson)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
            )

            val upsertStatus = conn.prepareStatement(
                "INSERT OR REPLACE INTO competition_fetch_status (competitionId, status) VALUES (?, ?)"
            )

            for (competitionId in 1L..maxCompetitionId) {
                if (competitionId in skipIds) continue

                val url = "https://webshooter.se/api/v4.1.9/competitions/$competitionId/results"
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .get()
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                try {
                    client.newCall(request).execute().use { response ->
                        when {
                            response.isSuccessful -> {
                                val body = response.body?.string() ?: ""
                                try {
                                    val json = com.google.gson.JsonParser.parseString(body).asJsonObject
                                    val results = json.getAsJsonArray("results")

                                    if (results == null || results.size() == 0) {
                                        upsertStatus.setLong(1, competitionId)
                                        upsertStatus.setString(2, "no_results")
                                        upsertStatus.execute()
                                        logger.lifecycle("Competition $competitionId: no results")
                                    } else {
                                        for (result in results) {
                                            val r = result.asJsonObject
                                            insertResult.setLong(1, r.get("id").asLong)
                                            insertResult.setLong(2, competitionId)
                                            insertResult.setLong(3, r.get("signups_id").asLong)
                                            insertResult.setLong(4, r.get("placement").asLong)
                                            insertResult.setLong(5, r.get("figure_hits").asLong)
                                            insertResult.setLong(6, r.get("hits").asLong)
                                            insertResult.setLong(7, r.get("points").asLong)
                                            val stdMedal = r.get("std_medal")
                                            insertResult.setString(
                                                8,
                                                if (stdMedal == null || stdMedal.isJsonNull) null
                                                else stdMedal.asString
                                            )
                                            insertResult.setString(9, gson.toJson(r.get("signup")))
                                            insertResult.setString(10, gson.toJson(r.get("weaponclass")))
                                            insertResult.setString(11, gson.toJson(r.get("results")))
                                            insertResult.execute()
                                        }
                                        upsertStatus.setLong(1, competitionId)
                                        upsertStatus.setString(2, "has_results")
                                        upsertStatus.execute()
                                        logger.lifecycle("Competition $competitionId: fetched ${results.size()} results")
                                    }
                                } catch (e: com.google.gson.JsonSyntaxException) {
                                    upsertStatus.setLong(1, competitionId)
                                    upsertStatus.setString(2, "invalid")
                                    upsertStatus.execute()
                                    logger.lifecycle("Competition $competitionId: invalid JSON response")
                                }
                            }
                            response.code in 500..599 -> {
                                upsertStatus.setLong(1, competitionId)
                                upsertStatus.setString(2, "retry")
                                upsertStatus.execute()
                                logger.lifecycle("Competition $competitionId: server error ${response.code}, marked for retry")
                            }
                            else -> {
                                upsertStatus.setLong(1, competitionId)
                                upsertStatus.setString(2, "invalid")
                                upsertStatus.execute()
                                logger.lifecycle("Competition $competitionId: HTTP ${response.code}, marked invalid")
                            }
                        }
                    }
                    conn.commit()
                } catch (e: IOException) {
                    upsertStatus.setLong(1, competitionId)
                    upsertStatus.setString(2, "retry")
                    upsertStatus.execute()
                    conn.commit()
                    logger.lifecycle("Competition $competitionId: network error, marked for retry")
                }
            }

            insertResult.close()
            upsertStatus.close()

            // Final summary
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(
                    "SELECT status, COUNT(*) as cnt FROM competition_fetch_status GROUP BY status"
                )
                logger.lifecycle("--- Prebuilt Database Summary ---")
                while (rs.next()) {
                    logger.lifecycle("  ${rs.getString("status")}: ${rs.getInt("cnt")}")
                }
                val resultCount = conn.createStatement()
                    .executeQuery("SELECT COUNT(*) as cnt FROM results")
                resultCount.next()
                logger.lifecycle("  Total results: ${resultCount.getInt("cnt")}")
            }
        }
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

    sourceSets {
        getByName("main").java.srcDir(dbVersionDir)
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
        val secretField = "\"${clientSecret ?: "52FphTYzOrmuqH30ltL7LrBzhSEURIJiMFNp6Qt0"}\""

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

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Charts
    implementation(libs.mpandroidchart)

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
