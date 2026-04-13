package se.kjellstrand.webshooter.data.competitions.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Path

class CompetitionsRemoteDataSourceGetByIdTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `getCompetitionById method exists on the interface`() {
        val method = CompetitionsRemoteDataSource::class.java.methods
            .find { it.name == "getCompetitionById" }
        assertNotNull("CompetitionsRemoteDataSource should have getCompetitionById", method)
    }

    @Test
    fun `getCompetitionById is a suspend fun`() {
        val method = CompetitionsRemoteDataSource::class.java.methods
            .find { it.name == "getCompetitionById" }
        assertNotNull("getCompetitionById should exist", method)
        assertTrue(
            "getCompetitionById should be a suspend fun (has Continuation parameter)",
            method!!.parameterTypes.any { it.name.contains("Continuation") }
        )
    }

    @Test
    fun `getCompetitionById has GET annotation for competitions id path`() {
        val method = CompetitionsRemoteDataSource::class.java.methods
            .find { it.name == "getCompetitionById" }
        assertNotNull("getCompetitionById should exist", method)
        val getAnnotation = method!!.getAnnotation(GET::class.java)
        assertNotNull("getCompetitionById should have @GET annotation", getAnnotation)
        assertTrue(
            "GET path should reference competitions/{id}, was: ${getAnnotation!!.value}",
            getAnnotation.value.contains("competitions/{id}")
        )
    }

    @Test
    fun `getCompetitionById has Path annotated id parameter`() {
        val method = CompetitionsRemoteDataSource::class.java.methods
            .find { it.name == "getCompetitionById" }
        assertNotNull("getCompetitionById should exist", method)
        val pathAnnotation = method!!.parameterAnnotations
            .flatMap { it.toList() }
            .filterIsInstance<Path>()
            .firstOrNull()
        assertNotNull("getCompetitionById should have a @Path annotated parameter", pathAnnotation)
        assertEquals("id", pathAnnotation!!.value)
    }

    @Test
    fun `CompetitionByIdResponse class exists in remote package`() {
        val cls = try {
            Class.forName("se.kjellstrand.webshooter.data.competitions.remote.CompetitionByIdResponse")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("CompetitionByIdResponse should exist in the remote package", cls)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `getCompetitions method still exists`() {
        val method = CompetitionsRemoteDataSource::class.java.methods
            .find { it.name == "getCompetitions" }
        assertNotNull("CompetitionsRemoteDataSource should still have getCompetitions", method)
    }

    @Test
    fun `getCompetitions is still a suspend fun`() {
        val method = CompetitionsRemoteDataSource::class.java.methods
            .find { it.name == "getCompetitions" }
        assertNotNull("getCompetitions should exist", method)
        assertTrue(
            "getCompetitions should be a suspend fun",
            method!!.parameterTypes.any { it.name.contains("Continuation") }
        )
    }

    @Test
    fun `getCompetitions still has GET annotation`() {
        val method = CompetitionsRemoteDataSource::class.java.methods
            .find { it.name == "getCompetitions" }
        assertNotNull("getCompetitions should exist", method)
        val getAnnotation = method!!.getAnnotation(GET::class.java)
        assertNotNull("getCompetitions should have @GET annotation", getAnnotation)
    }
}
