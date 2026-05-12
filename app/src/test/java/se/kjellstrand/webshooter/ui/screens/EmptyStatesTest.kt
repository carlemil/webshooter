package se.kjellstrand.webshooter.ui.screens

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class EmptyStatesTest {

    private val clubScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/club/ClubScreen.kt")
    private val resultsScreen = File("src/main/java/se/kjellstrand/webshooter/ui/screens/results/ResultsScreen.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `ClubMemberListTab shows empty state message instead of spinner for empty list`() {
        val source = clubScreen.readText()
        val memberListSection = source.substringAfter("ClubMemberListTab")
        assertTrue(
            "ClubMemberListTab should show an empty state text message when members list is empty",
            memberListSection.contains("Res.string.club_no_members") ||
                memberListSection.contains("no_members") ||
                memberListSection.contains("No members")
        )
    }

    @Test
    fun `ClubMemberListTab does not show spinner for empty members`() {
        val source = clubScreen.readText()
        val memberListSection = source.substringAfter("fun ClubMemberListTab")
        val emptyBranch = memberListSection.substringAfter("isEmpty()")
            .substringBefore("LazyColumn")
        assertFalse(
            "ClubMemberListTab should not show CircularProgressIndicator when members list is empty",
            emptyBranch.contains("CircularProgressIndicator")
        )
    }

    @Test
    fun `ResultsList shows empty state when no weapon groups are selected`() {
        val source = resultsScreen.readText()
        val noneSelectedSection = source.substringAfter("noneSelected")
            .substringBefore("isLoading")
        assertTrue(
            "ResultsList should show an empty state message when no weapon groups are selected",
            noneSelectedSection.contains("Res.string.results_no_groups_selected") ||
                noneSelectedSection.contains("Text(") ||
                noneSelectedSection.contains("no_groups")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ClubScreen exists`() {
        assertTrue(clubScreen.exists())
    }

    @Test
    fun `ResultsScreen exists`() {
        assertTrue(resultsScreen.exists())
    }

    @Test
    fun `ClubScreen has ClubMemberListTab composable`() {
        val source = clubScreen.readText()
        assertTrue(source.contains("ClubMemberListTab"))
    }

    @Test
    fun `ResultsScreen has weapon group filter logic`() {
        val source = resultsScreen.readText()
        assertTrue(source.contains("selectedWeaponGroups"))
    }
}
