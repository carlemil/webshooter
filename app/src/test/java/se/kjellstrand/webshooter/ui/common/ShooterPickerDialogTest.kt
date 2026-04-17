package se.kjellstrand.webshooter.ui.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ShooterPickerDialogTest {

    private val sourceFile =
        File("src/main/java/se/kjellstrand/webshooter/ui/common/ShooterPickerDialog.kt")

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `selected section is rendered in a LazyColumn not a forEach`() {
        val source = sourceFile.readText()
        assertTrue(
            "Selected shooters must be rendered in a LazyColumn so the list scrolls independently",
            Regex("""LazyColumn[\s\S]*items\s*\(\s*selectedShooters""").containsMatchIn(source) ||
                source.contains("items(selectedShooters.entries") ||
                source.contains("items(selectedShooters.toList()")
        )
        assertFalse(
            "Old selectedShooters.forEach { Row { ... } } must be removed",
            Regex("""selectedShooters\.forEach\s*\{""").containsMatchIn(source)
        )
    }

    @Test
    fun `dialog caps both lists to half screen height`() {
        val source = sourceFile.readText()
        assertTrue(
            "Must derive a half-screen max height from LocalConfiguration.current.screenHeightDp",
            source.contains("LocalConfiguration.current.screenHeightDp")
        )
        val heightInCount = Regex("""heightIn\s*\(\s*max\s*=""").findAll(source).count()
        assertTrue(
            "Both the selected and the available LazyColumns must apply heightIn(max = ...); found $heightInCount",
            heightInCount >= 2
        )
    }

    @Test
    fun `available list no longer uses fixed 300 dp height`() {
        val source = sourceFile.readText()
        assertFalse(
            "The available list must not use the old fixed 300.dp height anymore",
            source.contains("height(300.dp)")
        )
    }

    @Test
    fun `dialog accepts optional relevantUserIds filter parameter`() {
        val source = sourceFile.readText()
        assertTrue(
            "ShooterPickerDialog must accept optional relevantUserIds: Set<Long>? = null",
            source.contains("relevantUserIds: Set<Long>? = null") ||
                source.contains("relevantUserIds: Set<Long>?")
        )
        assertTrue(
            "Filter logic must apply relevantUserIds when non-null",
            source.contains("relevantUserIds") &&
                (source.contains("it.userId in relevantUserIds") ||
                    source.contains("userId in relevantUserIds") ||
                    source.contains("relevantUserIds.contains"))
        )
    }

    @Test
    fun `selected rows are compacted not wrapped in IconButton`() {
        val source = sourceFile.readText()
        assertFalse(
            "Selected rows must not use IconButton(onClick = { onRemoveShooter ... }) — too much padding",
            Regex("""IconButton\s*\(\s*onClick\s*=\s*\{\s*onRemoveShooter""").containsMatchIn(source)
        )
        assertTrue(
            "Selected row remove icon must be a small clickable Icon sized ~20.dp",
            Regex("""Modifier\s*\.\s*size\s*\(\s*20\.dp\s*\)\s*\.\s*clickable""").containsMatchIn(source) ||
                Regex("""\.size\(20\.dp\)[\s\S]*\.clickable""").containsMatchIn(source)
        )
        assertTrue(
            "Selected row label should use bodySmall typography for compactness",
            source.contains("typography.bodySmall")
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `ShooterPickerDialog composable exists with original public signature`() {
        val source = sourceFile.readText()
        assertTrue(source.contains("fun ShooterPickerDialog("))
        assertTrue(source.contains("title: String"))
        assertTrue(source.contains("searchQuery: String"))
        assertTrue(source.contains("clubMembers: List<ClubMember>"))
        assertTrue(source.contains("allParticipants: List<Participant>"))
        assertTrue(source.contains("selectedShooters: Map<Long, String>"))
        assertTrue(source.contains("onPickShooter: (Long) -> Unit"))
        assertTrue(source.contains("onRemoveShooter: (Long) -> Unit"))
        assertTrue(source.contains("showSelectedSection: Boolean"))
    }

    @Test
    fun `dialog still uses AlertDialog and OutlinedTextField for search`() {
        val source = sourceFile.readText()
        assertTrue(source.contains("AlertDialog("))
        assertTrue(source.contains("OutlinedTextField("))
    }

    @Test
    fun `filter logic preserves search + sort + exclude-selected ordering`() {
        val source = sourceFile.readText()
        assertTrue(source.contains("selectedShooters.containsKey"))
        assertTrue(source.contains("sortedBy"))
    }
}
