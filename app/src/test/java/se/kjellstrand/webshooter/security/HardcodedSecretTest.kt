package se.kjellstrand.webshooter.security

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class HardcodedSecretTest {

    private val loginRepo = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/data/login/LoginRepository.kt")
    private val refreshTokenRequest = File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/data/login/remote/RefreshTokenRequest.kt")
    private val buildGradle = File("build.gradle.kts")
    private val prebuiltDbGradle = File("prebuilt-database.gradle.kts")
    private val iosProject = File("../iosApp/project.yml")

    // The webshooter.se OAuth client secret is a quoted 40-char alphanumeric literal.
    // It must come from local.properties / Secrets.xcconfig, never from tracked files.
    private val secretLiteral = Regex("\"[A-Za-z0-9]{40}\"")

    private fun assertNoHardcodedSecret(file: File) {
        assertTrue("${file.path} should exist", file.exists())
        assertFalse(
            "${file.path} should not contain a hardcoded client secret",
            secretLiteral.containsMatchIn(file.readText())
        )
    }

    @Test
    fun `LoginRepository does not contain hardcoded client secret`() = assertNoHardcodedSecret(loginRepo)

    @Test
    fun `RefreshTokenRequest does not contain hardcoded client secret`() = assertNoHardcodedSecret(refreshTokenRequest)

    @Test
    fun `app build script does not contain hardcoded client secret`() = assertNoHardcodedSecret(buildGradle)

    @Test
    fun `prebuilt database script does not contain hardcoded client secret`() = assertNoHardcodedSecret(prebuiltDbGradle)

    @Test
    fun `iOS project does not contain hardcoded client secret`() = assertNoHardcodedSecret(iosProject)

    @Test
    fun `client secret is defined in BuildConfig`() {
        assertTrue(
            "build.gradle.kts should define CLIENT_SECRET as a buildConfigField",
            buildGradle.readText().contains("CLIENT_SECRET")
        )
    }

    @Test
    fun `LoginRepository still creates LoginRequest`() {
        assertTrue(
            "LoginRepository should still create LoginRequest for authentication",
            loginRepo.readText().contains("LoginRequest(")
        )
    }
}
