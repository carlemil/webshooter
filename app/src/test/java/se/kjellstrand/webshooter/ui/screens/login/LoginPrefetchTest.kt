package se.kjellstrand.webshooter.ui.screens.login

import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import java.io.File

class LoginPrefetchTest {

    private val loginSource: String by lazy {
        File("../shared/src/commonMain/kotlin/se/kjellstrand/webshooter/ui/screens/login/LoginViewModelImpl.kt").readText()
    }

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `LoginViewModelImpl constructor accepts CompetitionsRepository`() {
        val constructors = LoginViewModelImpl::class.java.declaredConstructors
        val hasCompetitionsRepo = constructors.any { constructor ->
            constructor.parameterTypes.any { it == CompetitionsRepository::class.java }
        }
        assertTrue(
            "LoginViewModelImpl should have CompetitionsRepository in its constructor",
            hasCompetitionsRepo
        )
    }

    @Test
    fun `LoginViewModelImpl has competitionsRepository field`() {
        val field = LoginViewModelImpl::class.java.declaredFields.find {
            it.type == CompetitionsRepository::class.java
        }
        assertNotNull(
            "LoginViewModelImpl should have a CompetitionsRepository field",
            field
        )
    }

    @Test
    fun `LoginViewModelImpl declares prefetchCompetitions method`() {
        val method = LoginViewModelImpl::class.java.declaredMethods.find {
            it.name == "prefetchCompetitions"
        }
        assertNotNull(
            "LoginViewModelImpl should have a prefetchCompetitions method (renamed from prefetchCompletedCompetitions)",
            method
        )
    }

    @Test
    fun `LoginViewModelImpl no longer has prefetchCompletedCompetitions`() {
        val method = LoginViewModelImpl::class.java.declaredMethods.find {
            it.name == "prefetchCompletedCompetitions"
        }
        assertNull(
            "prefetchCompletedCompetitions should be renamed to prefetchCompetitions",
            method
        )
    }

    @Test
    fun `LoginViewModelImpl source calls syncAll on the repository`() {
        assertTrue(
            "LoginViewModelImpl source should call competitionsRepository.syncAll()",
            loginSource.contains("competitionsRepository.syncAll()")
        )
    }

    @Test
    fun `LoginViewModelImpl source no longer calls syncCompleted directly`() {
        assertFalse(
            "LoginViewModelImpl should not call syncCompleted() — that's now an internal step of syncAll()",
            loginSource.contains("competitionsRepository.syncCompleted()")
        )
    }

    @Test
    fun `LoginViewModelImpl constructor accepts a CoroutineScope`() {
        val constructors = LoginViewModelImpl::class.java.declaredConstructors
        val hasScope = constructors.any { constructor ->
            constructor.parameterTypes.any { CoroutineScope::class.java.isAssignableFrom(it) }
        }
        assertTrue(
            "LoginViewModelImpl should accept a CoroutineScope in its constructor (@ApplicationScope-injected)",
            hasScope
        )
    }

    @Test
    fun `LoginViewModelImpl has a CoroutineScope field`() {
        val field = LoginViewModelImpl::class.java.declaredFields.find {
            CoroutineScope::class.java.isAssignableFrom(it.type)
        }
        assertNotNull(
            "LoginViewModelImpl should store its injected CoroutineScope as a field",
            field
        )
    }

    @Test
    fun `prefetchCompetitions source launches on applicationScope not viewModelScope`() {
        // Extract the body of prefetchCompetitions() only — bounded by the next
        // fun/override declaration so we don't leak into getCookies() which still
        // uses viewModelScope by design.
        val start = loginSource.indexOf("private fun prefetchCompetitions()")
        assertTrue("prefetchCompetitions() should exist in source", start >= 0)
        val afterStart = start + "private fun prefetchCompetitions()".length
        val nextFun = listOf(
            loginSource.indexOf("private fun ", afterStart),
            loginSource.indexOf("override fun ", afterStart),
            loginSource.indexOf("companion object", afterStart)
        ).filter { it >= 0 }.minOrNull() ?: loginSource.length
        val body = loginSource.substring(start, nextFun)
        assertTrue(
            "prefetchCompetitions() body should launch on applicationScope, was:\n$body",
            body.contains("applicationScope.launch")
        )
        assertFalse(
            "prefetchCompetitions() body should NOT launch on viewModelScope (it gets cancelled when login screen pops), was:\n$body",
            body.contains("viewModelScope.launch")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `LoginViewModelImpl still has login method`() {
        val method = LoginViewModelImpl::class.java.methods.find { it.name == "login" }
        assertNotNull("LoginViewModelImpl should still have login method", method)
    }

    @Test
    fun `LoginViewModelImpl still has getCookies method`() {
        val method = LoginViewModelImpl::class.java.methods.find { it.name == "getCookies" }
        assertNotNull("LoginViewModelImpl should still have getCookies method", method)
    }

    @Test
    fun `LoginViewModelImpl implements LoginViewModel`() {
        val interfaceClass = LoginViewModel::class.java
        assertTrue(
            "LoginViewModelImpl should implement LoginViewModel",
            interfaceClass.isAssignableFrom(LoginViewModelImpl::class.java)
        )
    }
}
