package se.kjellstrand.webshooter.data

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ProductionLoggingTest {

    private val srcDir = File("src/main/java/se/kjellstrand/webshooter")

    private fun allKtFiles(): List<File> = srcDir.walkTopDown().filter { it.extension == "kt" }.toList()

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `no println calls in production code`() {
        val violations = allKtFiles().flatMap { file ->
            file.readLines().mapIndexedNotNull { i, line ->
                if (line.contains("println(") && !line.trimStart().startsWith("//"))
                    "${file.name}:${i + 1}: $line" else null
            }
        }
        assertTrue(
            "Found println() calls in production code:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }

    @Test
    fun `no printStackTrace calls in production code`() {
        val violations = allKtFiles().flatMap { file ->
            file.readLines().mapIndexedNotNull { i, line ->
                if (line.contains("printStackTrace()") && !line.trimStart().startsWith("//"))
                    "${file.name}:${i + 1}: $line" else null
            }
        }
        assertTrue(
            "Found printStackTrace() calls in production code:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }

    @Test
    fun `no Log_d calls in production code`() {
        val violations = allKtFiles().flatMap { file ->
            file.readLines().mapIndexedNotNull { i, line ->
                if (line.contains("Log.d(") && !line.trimStart().startsWith("//"))
                    "${file.name}:${i + 1}: $line" else null
            }
        }
        assertTrue(
            "Found Log.d() calls in production code:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `source directory exists`() {
        assertTrue(srcDir.exists())
    }

    @Test
    fun `kotlin source files exist`() {
        assertTrue(allKtFiles().isNotEmpty())
    }
}
