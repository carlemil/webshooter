package se.kjellstrand.webshooter.security

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ProguardRulesTest {

    private val proguardFile = File("proguard-rules.pro")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `proguard has keep rules for Retrofit`() {
        val content = proguardFile.readText()
        assertTrue(
            "ProGuard should have keep rules for Retrofit interfaces",
            content.contains("retrofit2") && content.contains("-keep")
        )
    }

    @Test
    fun `proguard has keep rules for Gson serialization`() {
        val content = proguardFile.readText()
        assertTrue(
            "ProGuard should have keep rules for Gson serialized classes",
            content.contains("Gson") || content.contains("gson") || content.contains("SerializedName")
        )
    }

    @Test
    fun `proguard has keep rules for Room entities`() {
        val content = proguardFile.readText()
        assertTrue(
            "ProGuard should have keep rules for Room",
            content.contains("room") || content.contains("Room") || content.contains("androidx.room")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `proguard file exists`() {
        assertTrue(proguardFile.exists())
    }

    @Test
    fun `proguard file is not empty`() {
        assertTrue(proguardFile.readText().isNotBlank())
    }
}
