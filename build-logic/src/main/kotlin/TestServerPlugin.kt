import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.testing.Test
import java.util.concurrent.Executors
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.SimpleFileServer
import java.net.InetSocketAddress
import java.nio.file.Path

abstract class TestServerExtension {
    abstract val contentDirectory: DirectoryProperty
}

class TestServerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("testServer", TestServerExtension::class.java)
        var server: HttpServer? = null

        project.tasks.withType(Test::class.java).configureEach {
            dependsOn(project.tasks.named("bake"))

            doFirst {
                project.logger.lifecycle("Starting in-process test server...")
                val contentPath = extension.contentDirectory.get().asFile.toPath()
                server = HttpServer.create(
                    InetSocketAddress(0), // 0 means a random available port
                    0,
                    "/",
                    SimpleFileServer.createFileHandler(contentPath)
                ).apply {
                    executor = Executors.newSingleThreadExecutor()
                    start()
                }
                val testServerPort = server?.address?.port
                System.setProperty("test.server.port", testServerPort.toString())
                project.logger.lifecycle("In-process test server started on port $testServerPort")
            }

            doLast {
                project.logger.lifecycle("Stopping in-process test server...")
                server?.stop(0)
                System.clearProperty("test.server.port")
            }
        }
    }
}
