package se.kjellstrand.webshooter.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.ui.screens.club.ClubTab
import se.kjellstrand.webshooter.ui.screens.club.ClubUiState
import se.kjellstrand.webshooter.ui.screens.settings.SettingsTab
import se.kjellstrand.webshooter.ui.screens.settings.SettingsUiState
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultUiState
import se.kjellstrand.webshooter.ui.screens.signup.SignupUiState
import java.io.File

/**
 * After the KMP migration, ViewModel interfaces and impls live in
 * `:shared/commonMain` — `:app` only contains the Compose screens that
 * consume them. This test still enforces the interface/impl split via
 * reflection (which works because :shared is on the test classpath) and
 * verifies the screen files reference the impl types correctly.
 */
class ViewModelInterfaceExtractionTest {

    private val screenBaseDir = "../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens"

    @Test
    fun `ClubViewModel should be an interface`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.club.ClubViewModel")
        assertTrue("ClubViewModel should be an interface", clazz.isInterface)
    }

    @Test
    fun `ClubViewModelImpl should exist and implement ClubViewModel`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.screens.club.ClubViewModelImpl")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.club.ClubViewModel")
        assertTrue(
            "ClubViewModelImpl should implement ClubViewModel",
            interfaceClass.isAssignableFrom(implClass)
        )
    }

    @Test
    fun `SettingsViewModel should be an interface`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.settings.SettingsViewModel")
        assertTrue("SettingsViewModel should be an interface", clazz.isInterface)
    }

    @Test
    fun `SettingsViewModelImpl should exist and implement SettingsViewModel`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.screens.settings.SettingsViewModelImpl")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.settings.SettingsViewModel")
        assertTrue(
            "SettingsViewModelImpl should implement SettingsViewModel",
            interfaceClass.isAssignableFrom(implClass)
        )
    }

    @Test
    fun `ShooterResultViewModel should be an interface`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultViewModel")
        assertTrue("ShooterResultViewModel should be an interface", clazz.isInterface)
    }

    @Test
    fun `ShooterResultViewModelImpl should exist and implement ShooterResultViewModel`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultViewModelImpl")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultViewModel")
        assertTrue(
            "ShooterResultViewModelImpl should implement ShooterResultViewModel",
            interfaceClass.isAssignableFrom(implClass)
        )
    }

    @Test
    fun `SignupViewModel should be an interface`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.signup.SignupViewModel")
        assertTrue("SignupViewModel should be an interface", clazz.isInterface)
    }

    @Test
    fun `SignupViewModelImpl should exist and implement SignupViewModel`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.screens.signup.SignupViewModelImpl")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.signup.SignupViewModel")
        assertTrue(
            "SignupViewModelImpl should implement SignupViewModel",
            interfaceClass.isAssignableFrom(implClass)
        )
    }

    @Test
    fun `ClubScreen should reference ClubViewModelImpl in koinViewModel`() {
        val source = File("$screenBaseDir/club/ClubScreen.kt").readText()
        assertTrue(
            "ClubScreen should use koinViewModel<ClubViewModelImpl>()",
            source.contains("koinViewModel<ClubViewModelImpl>()")
        )
    }

    @Test
    fun `SettingsScreen should reference SettingsViewModelImpl in koinViewModel`() {
        val source = File("$screenBaseDir/settings/SettingsScreen.kt").readText()
        assertTrue(
            "SettingsScreen should use koinViewModel<SettingsViewModelImpl>()",
            source.contains("koinViewModel<SettingsViewModelImpl>()")
        )
    }

    // --- Guard tests (default UiState invariants) ---

    @Test
    fun `ClubUiState has correct defaults`() {
        val state = ClubUiState()
        assertFalse(state.isLoading)
        assertNull(state.clubData)
        assertNull(state.error)
        assertEquals(ClubTab.INFORMATION, state.selectedTab)
    }

    @Test
    fun `SettingsUiState has correct defaults`() {
        val state = SettingsUiState()
        assertFalse(state.isLoading)
        assertNull(state.profile)
        assertFalse(state.isEditMode)
        assertEquals(SettingsTab.PROFILE, state.selectedTab)
        assertFalse(state.loggedOut)
    }

    @Test
    fun `ShooterResultUiState has correct defaults`() {
        val state = ShooterResultUiState()
        assertTrue(state.isLoading)
        assertEquals("", state.shooterName)
        assertTrue(state.results.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `SignupUiState has correct defaults`() {
        val state = SignupUiState()
        assertFalse(state.isLoading)
        assertNull(state.selectedWeaponClassId)
        assertEquals("", state.note)
        assertFalse(state.isSuccess)
        assertNull(state.error)
    }
}
