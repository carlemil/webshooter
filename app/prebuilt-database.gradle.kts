buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.xerial:sqlite-jdbc:3.47.1.0")
        classpath("com.squareup.okhttp3:okhttp:4.12.0")
        classpath("com.google.code.gson:gson:2.11.0")
    }
}

import java.io.IOException
import java.sql.DriverManager
import java.util.Properties
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType

val appVersionCode: Int by project.extra

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

                Thread.sleep(10_000)
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

tasks.configureEach {
    if (name == "assembleProdRelease" || name == "bundleProdRelease") {
        dependsOn(generatePrebuiltDatabase)
    }
}
