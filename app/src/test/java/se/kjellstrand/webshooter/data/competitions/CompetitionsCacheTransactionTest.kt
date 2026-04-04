package se.kjellstrand.webshooter.data.competitions

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kjellstrand.webshooter.data.competitions.local.CompetitionsDao

class CompetitionsCacheTransactionTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `DAO has replaceAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "replaceAll" }
        assertNotNull("CompetitionsDao should have a replaceAll method", method)
    }

    @Test
    fun `replaceAll accepts list parameter`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "replaceAll" }
        assertNotNull("CompetitionsDao should have a replaceAll method", method)
        val params = method!!.parameterTypes
        assertTrue(
            "replaceAll should accept a List parameter",
            params.any { it == List::class.java || it == java.util.List::class.java }
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `DAO has getAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "getAll" }
        assertNotNull("CompetitionsDao should have a getAll method", method)
    }

    @Test
    fun `DAO has deleteAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "deleteAll" }
        assertNotNull("CompetitionsDao should have a deleteAll method", method)
    }

    @Test
    fun `DAO has insertAll method`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "insertAll" }
        assertNotNull("CompetitionsDao should have a insertAll method", method)
    }

    @Test
    fun `insertAll accepts list parameter`() {
        val method = CompetitionsDao::class.java.methods.find { it.name == "insertAll" }
        assertNotNull(method)
        val params = method!!.parameterTypes
        assertTrue(
            "insertAll should accept a List parameter",
            params.any { it == List::class.java || it == java.util.List::class.java }
        )
    }
}
