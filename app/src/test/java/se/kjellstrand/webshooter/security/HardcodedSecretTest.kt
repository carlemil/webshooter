package se.kjellstrand.webshooter.security

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class HardcodedSecretTest {

    private val loginRepo = File("src/main/java/se/kjellstrand/webshooter/data/login/LoginRepository.kt")
    private val refreshTokenRequest = File("src/main/java/se/kjellstrand/webshooter/data/login/remote/RefreshTokenRequest.kt")
    private val buildGradle = File("build.gradle.kts")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `LoginRepository does not contain hardcoded client secret`() {
        val source = loginRepo.readText()
        assertFalse(
            "LoginRepository should not contain the hardcoded client secret string",
            source.contains("REMOVED-CLIENT-SECRET")
        )
    }

    @Test
    fun `RefreshTokenRequest does not contain hardcoded client secret`() {
        val source = refreshTokenRequest.readText()
        assertFalse(
            "RefreshTokenRequest should not contain the hardcoded client secret string",
            source.contains("REMOVED-CLIENT-SECRET")
        )
    }

    @Test
    fun `client secret is defined in BuildConfig`() {
        val source = buildGradle.readText()
        assertTrue(
            "build.gradle.kts should define CLIENT_SECRET as a buildConfigField",
            source.contains("CLIENT_SECRET")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `LoginRepository exists`() {
        assertTrue(loginRepo.exists())
    }

    @Test
    fun `RefreshTokenRequest exists`() {
        assertTrue(refreshTokenRequest.exists())
    }

    @Test
    fun `LoginRepository still creates LoginRequest`() {
        val source = loginRepo.readText()
        assertTrue(
            "LoginRepository should still create LoginRequest for authentication",
            source.contains("LoginRequest(")
        )
    }
}
