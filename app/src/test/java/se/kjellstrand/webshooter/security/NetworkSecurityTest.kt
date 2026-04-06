package se.kjellstrand.webshooter.security

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class NetworkSecurityTest {

    private val configFile = File("src/main/res/xml/network_security_config.xml")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `webshooter_se does not permit cleartext traffic`() {
        val content = configFile.readText()
        val hasCleartextWithWebshooter = content.contains("cleartextTrafficPermitted=\"true\"") &&
            content.contains("webshooter.se")
        assertFalse(
            "webshooter.se should not be in a cleartextTrafficPermitted=true domain-config",
            hasCleartextWithWebshooter
        )
    }

    @Test
    fun `main config does not globally allow cleartext`() {
        val content = configFile.readText()
        assertFalse(
            "Should not have base-config with cleartext permitted",
            content.contains("<base-config cleartextTrafficPermitted=\"true\"")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `network security config exists`() {
        assertTrue(configFile.exists())
    }

    @Test
    fun `config is valid XML`() {
        val content = configFile.readText()
        assertTrue(content.contains("network-security-config"))
    }
}
