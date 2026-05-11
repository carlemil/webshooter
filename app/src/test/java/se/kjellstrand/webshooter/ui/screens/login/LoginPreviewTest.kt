package se.kjellstrand.webshooter.ui.screens.login

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class LoginPreviewTest {

    private val loginScreenFile = File("src/main/java/se/kjellstrand/webshooter/ui/screens/login/LoginScreen.kt")
    private val mockDir = "src/main/java/se/kjellstrand/webshooter/ui/mock"

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `LoginViewModel should be an interface`() {
        val clazz = Class.forName("se.kjellstrand.webshooter.ui.screens.login.LoginViewModel")
        assertTrue("LoginViewModel should be an interface", clazz.isInterface)
    }

    @Test
    fun `LoginViewModelImpl should exist and implement LoginViewModel`() {
        val implClass = Class.forName("se.kjellstrand.webshooter.ui.screens.login.LoginViewModelImpl")
        val interfaceClass = Class.forName("se.kjellstrand.webshooter.ui.screens.login.LoginViewModel")
        assertTrue(interfaceClass.isAssignableFrom(implClass))
    }

    @Test
    fun `LoginViewModelMock file should exist`() {
        assertTrue("LoginViewModelMock.kt should exist", File("$mockDir/LoginViewModelMock.kt").exists())
    }

    @Test
    fun `LoginScreen should have loading preview`() {
        val source = loginScreenFile.readText()
        assertTrue("Should have LoginScreenLoadingPreview", source.contains("LoginScreenLoadingPreview"))
    }

    @Test
    fun `LoginScreen should have error preview`() {
        val source = loginScreenFile.readText()
        assertTrue("Should have LoginScreenErrorPreview", source.contains("LoginScreenErrorPreview"))
    }

    @Test
    fun `LoginScreen should use LoginViewModelImpl in koinViewModel`() {
        val source = loginScreenFile.readText()
        assertTrue(
            "LoginScreen should use koinViewModel<LoginViewModelImpl>()",
            source.contains("koinViewModel<LoginViewModelImpl>()")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `LoginScreen file should exist`() {
        assertTrue(loginScreenFile.exists())
    }

    @Test
    fun `LoginScreen should have at least one Preview annotation`() {
        val source = loginScreenFile.readText()
        assertTrue(source.contains("@Preview"))
    }

    @Test
    fun `LoginUiState should have correct defaults`() {
        val state = LoginUiState()
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.errorMessage)
        assertFalse(state.autoLoginAttempted)
    }
}
