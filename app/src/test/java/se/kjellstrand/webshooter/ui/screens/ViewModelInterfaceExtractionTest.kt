package se.kjellstrand.webshooter.ui.screens

import org.junit.Assert.*
import org.junit.Test
import se.kjellstrand.webshooter.ui.screens.club.ClubUiState
import se.kjellstrand.webshooter.ui.screens.club.ClubTab
import se.kjellstrand.webshooter.ui.screens.settings.SettingsUiState
import se.kjellstrand.webshooter.ui.screens.settings.SettingsTab
import se.kjellstrand.webshooter.ui.screens.shooterresult.ShooterResultUiState
import se.kjellstrand.webshooter.ui.screens.signup.SignupUiState
import java.io.File

class ViewModelInterfaceExtractionTest {

    private val baseDir = "src/main/java/se/kjellstrand/webshooter/ui/screens"

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

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
    fun `ClubScreen should reference ClubViewModelImpl in hiltViewModel`() {
        val source = File("$baseDir/club/ClubScreen.kt").readText()
        assertTrue(
            "ClubScreen should use hiltViewModel<ClubViewModelImpl>()",
            source.contains("hiltViewModel<ClubViewModelImpl>()")
        )
    }

    @Test
    fun `SettingsScreen should reference SettingsViewModelImpl in hiltViewModel`() {
        val source = File("$baseDir/settings/SettingsScreen.kt").readText()
        assertTrue(
            "SettingsScreen should use hiltViewModel<SettingsViewModelImpl>()",
            source.contains("hiltViewModel<SettingsViewModelImpl>()")
        )
    }

    @Test
    fun `ShooterResultScreen should reference ShooterResultViewModelImpl in hiltViewModel`() {
        val source = File("$baseDir/shooterresult/ShooterResultScreen.kt").readText()
        assertTrue(
            "ShooterResultScreen should use hiltViewModel<ShooterResultViewModelImpl>()",
            source.contains("hiltViewModel<ShooterResultViewModelImpl>()")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

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

    @Test
    fun `all ViewModel files exist`() {
        listOf(
            "club/ClubViewModel.kt",
            "settings/SettingsViewModel.kt",
            "shooterresult/ShooterResultViewModel.kt",
            "signup/SignupViewModel.kt"
        ).forEach { path ->
            assertTrue("$path should exist", File("$baseDir/$path").exists())
        }
    }
}
