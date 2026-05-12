package se.kjellstrand.webshooter.ui.mock

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class MockViewModelsTest {

    private val mockDir = "../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/mock"

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ClubViewModelMock file should exist`() {
        assertTrue(File("$mockDir/ClubViewModelMock.kt").exists())
    }

    @Test
    fun `MyResultsViewModelMock file should exist`() {
        assertTrue(File("$mockDir/MyResultsViewModelMock.kt").exists())
    }

    @Test
    fun `PatrolsViewModelMock file should exist`() {
        assertTrue(File("$mockDir/PatrolsViewModelMock.kt").exists())
    }

    @Test
    fun `SettingsViewModelMock file should exist`() {
        assertTrue(File("$mockDir/SettingsViewModelMock.kt").exists())
    }

    @Test
    fun `ShooterResultViewModelMock file should exist`() {
        assertTrue(File("$mockDir/ShooterResultViewModelMock.kt").exists())
    }

    @Test
    fun `SignupViewModelMock file should exist`() {
        assertTrue(File("$mockDir/SignupViewModelMock.kt").exists())
    }

    @Test
    fun `SignupsViewModelMock file should exist`() {
        assertTrue(File("$mockDir/SignupsViewModelMock.kt").exists())
    }

    @Test
    fun `TeamsViewModelMock file should exist`() {
        assertTrue(File("$mockDir/TeamsViewModelMock.kt").exists())
    }

    @Test
    fun `ClubViewModelMock should implement ClubViewModel interface`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.mock.ClubViewModelMock")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.club.ClubViewModel")
        assertTrue("ClubViewModelMock should implement ClubViewModel", interfaceClass.isAssignableFrom(implClass))
    }

    @Test
    fun `SettingsViewModelMock should implement SettingsViewModel interface`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.mock.SettingsViewModelMock")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.settings.SettingsViewModel")
        assertTrue("SettingsViewModelMock should implement SettingsViewModel", interfaceClass.isAssignableFrom(implClass))
    }

    @Test
    fun `TeamsViewModelMock should implement TeamsViewModel interface`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.mock.TeamsViewModelMock")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.teams.TeamsViewModel")
        assertTrue("TeamsViewModelMock should implement TeamsViewModel", interfaceClass.isAssignableFrom(implClass))
    }

    @Test
    fun `SignupsViewModelMock should implement SignupsViewModel interface`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.mock.SignupsViewModelMock")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.signups.SignupsViewModel")
        assertTrue("SignupsViewModelMock should implement SignupsViewModel", interfaceClass.isAssignableFrom(implClass))
    }

    @Test
    fun `mock ViewModels should accept UiState constructor parameter`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.mock.ClubViewModelMock")
        val constructors = clazz.declaredConstructors
        val hasUiStateParam = constructors.any { c ->
            c.parameterTypes.any { it.simpleName == "ClubUiState" }
        }
        assertTrue("ClubViewModelMock should have a constructor with ClubUiState param", hasUiStateParam)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `CompetitionsViewModelMock should still exist and implement interface`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.mock.CompetitionsViewModelMock")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.competitions.CompetitionsViewModel")
        assertTrue(interfaceClass.isAssignableFrom(implClass))
    }

    @Test
    fun `ResultsViewModelMock should still exist and implement interface`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.mock.ResultsViewModelMock")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.results.ResultsViewModel")
        assertTrue(interfaceClass.isAssignableFrom(implClass))
    }

    @Test
    fun `existing mock ViewModel files should exist`() {
        assertTrue(File("$mockDir/CompetitionsViewModelMock.kt").exists())
        assertTrue(File("$mockDir/ResultsViewModelMock.kt").exists())
    }
}
