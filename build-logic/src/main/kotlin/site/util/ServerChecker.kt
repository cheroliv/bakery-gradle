package site.util

import java.net.HttpURLConnection
import java.net.URL

object ServerChecker {
    fun waitForServer(urlString: String, timeoutSeconds: Int = 30) {
        println("Waiting for server at $urlString to become available...")
        val url = URL(urlString)
        val endTime = System.currentTimeMillis() + timeoutSeconds * 1000
        var serverReady = false

        while (System.currentTimeMillis() < endTime) {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 1000
                connection.readTimeout = 1000
                if (connection.responseCode == 200) {
                    serverReady = true
                    println("Server is available.")
                    break
                }
            } catch (e: java.io.IOException) {
                // Ignore and retry
            }
            Thread.sleep(1000)
        }

        if (!serverReady) {
            throw org.gradle.api.GradleException("Server did not become available within $timeoutSeconds seconds.")
        }
    }
}
