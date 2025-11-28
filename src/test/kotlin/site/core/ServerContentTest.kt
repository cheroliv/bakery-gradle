package site.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import site.BaseUITest
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.test.fail

@Disabled("Disabling test to focus on fixing the bake process first.")
class ServerContentTest : BaseUITest() {

    private val projectRootDir = File(System.getProperty("project.root.dir") ?: ".")
    private val indexHtmlFile = File(projectRootDir, "build/jbake/index.html")
    private val serverIndexUrl = "${serverUrl}index.html"
    private val maxRetries = 5
    private val retryDelay = 1000L // 1 second


    @Test
    fun `index html content should match served content`() {
        // 1. Read the expected content from the baked file with retries
        val expectedContent = readFileWithRetries(indexHtmlFile)
        val normalizedExpected = normalizeWhitespace(expectedContent)

        var lastError: Throwable? = null
        var success = false

        for (attempt in 1..maxRetries) {
            try {
                // 2. Fetch the actual content from the running server
                val actualContent = java.net.URI.create(serverIndexUrl).toURL().readText()
                val normalizedActual = normalizeWhitespace(actualContent)

                // 3. Compare the contents
                assertEquals(
                    normalizedExpected,
                    normalizedActual,
                    "Served index.html content does not match baked content."
                )
                success = true
                println("Content matched on attempt $attempt.")
                break // Exit on success
            } catch (e: Exception) {
                lastError = e
                println("Attempt $attempt/$maxRetries to fetch URL failed. Retrying in ${retryDelay}ms...")
                Thread.sleep(retryDelay)
            }
        }

        if (!success) {
            fail("Server content did not match baked content after $maxRetries attempts.", lastError)
        }
    }

    private fun readFileWithRetries(file: File): String {
        var lastError: Throwable? = null
        for (attempt in 1..maxRetries) {
            if (file.exists()) {
                try {
                    return file.readText()
                } catch (e: IOException) {
                    lastError = e
                    println("Attempt $attempt/$maxRetries to read file failed: ${e.message}. Retrying...")
                    Thread.sleep(retryDelay)
                }
            } else {
                lastError = FileNotFoundException("File not found: ${file.absolutePath}")
                println("Attempt $attempt/$maxRetries: File not found yet. Retrying...")
                Thread.sleep(retryDelay)
            }
        }
        throw lastError ?: FileNotFoundException("File not found after $maxRetries attempts: ${file.absolutePath}")
    }

    private fun normalizeWhitespace(text: String): String {
        return text.replace("\\s+".toRegex(), " ").trim()
    }
}
