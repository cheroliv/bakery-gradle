import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import java.io.File

class ServePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("serve", Exec::class.java) {
            group = "managed-jbake"
            description = "Serves the JBake site using SimpleHttpServer (non-blocking)."
            dependsOn(project.tasks.named("bake")) // Ensure site is baked before serving
            val pidFile = project.layout.buildDirectory.file("jbake.pid")
            val serverPort = 8820

            doFirst {
                println("Starting Simple HTTP Server in background...")
                project.layout.buildDirectory.get().asFile.mkdirs()
                val outputDir = project.tasks.named("bake").get().outputs.files.singleFile
                val classpath = project.configurations.getByName("jbakeRuntime").asPath

                commandLine(
                    "sh", "-c",
                    "java -cp \"$classpath\" site.util.SimpleHttpServer $serverPort ${outputDir.absolutePath} ${pidFile.get().asFile.absolutePath} > ${
                        project.layout.buildDirectory.get().asFile.resolve(
                            "simple-http-server.log"
                        ).absolutePath
                    } 2>&1 & " +
                            "echo \$! > ${pidFile.get().asFile.absolutePath}"
                )

                workingDir(project.rootDir)
            }

            // The doLast block for waiting for the server to be available remains the same
            doLast {
                println("Waiting for server to become available at http://localhost:$serverPort...")
                val url = Class.forName("java.net.URL").getConstructor(String::class.java)
                    .newInstance("http://localhost:$serverPort") as java.net.URL
                val maxAttempts = 60 // 60 seconds timeout
                var attempts = 0
                var serverReady = false
                while (!serverReady && attempts < maxAttempts) {
                    try {
                        (url.openConnection() as java.net.HttpURLConnection).apply {
                            requestMethod = "GET"
                            connectTimeout = 1000
                            readTimeout = 1000
                            if (responseCode == 200) {
                                serverReady = true
                            } else {
                                Thread.sleep(1000)
                            }
                            disconnect()
                        }
                    } catch (e: java.net.ConnectException) {
                        Thread.sleep(1000)
                    } catch (e: Exception) {
                        println("An error occurred while checking server status: ${e.message}")
                    }
                    attempts++
                }
                if (serverReady) {
                    println("Server is up and running.")
                } else {
                    throw org.gradle.api.GradleException("Server failed to start within 60 seconds.")
                }
            }
        }

        project.tasks.register("stopServe") {
            group = "managed-jbake"
            description = "Stops the background Simple HTTP Server."
            val pidFile = project.layout.buildDirectory.file("jbake.pid")

            doFirst {
                if (pidFile.get().asFile.exists()) {
                    val pid = pidFile.get().asFile.readText().trim().toLong()
                    println("Attempting to kill process with PID $pid.")
                    try {
                        ProcessBuilder("kill", pid.toString()).start().waitFor()
                        println("Process $pid killed.")
                    } catch (e: Exception) {
                        println("Error killing process $pid: ${e.message}")
                    }
                } else {
                    println("PID file not found. Server might not be running or was already stopped.")
                }
            }

            doLast {
                println("Cleaning up PID file.")
                if (pidFile.get().asFile.exists()) {
                    if (pidFile.get().asFile.delete()) {
                        println("PID file deleted.")
                    } else {
                        println("Warning: Failed to delete PID file.")
                    }
                }
            }
        }
    }
}