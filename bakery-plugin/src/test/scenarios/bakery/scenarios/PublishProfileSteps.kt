package bakery.scenarios

import bakery.FileSystemManager.yamlMapper
import bakery.GitPushConfiguration
import bakery.RepositoryConfiguration
import bakery.RepositoryCredentials
import bakery.SiteConfiguration
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import org.gradle.testkit.runner.GradleRunner.create
import java.io.File
import java.nio.file.Files
import kotlin.text.Charsets.UTF_8

@Suppress("MemberVisibilityCanBePrivate")
class PublishProfileSteps(private val world: BakeryWorld) {

    @Given("an existing Bakery project with pushProfile configuration")
    fun createProjectWithPushProfile() {
        world.createGradleProject()
        val siteYml = world.projectDir!!.resolve("site.yml")
        val site = yamlMapper.readValue(siteYml, SiteConfiguration::class.java)
        val updatedSite = site.copy(
            pushProfile = GitPushConfiguration(
                from = "",
                to = "profile-cvs",
                repo = RepositoryConfiguration(
                    name = "cheroliv",
                    repository = "https://github.com/cheroliv/cheroliv.git",
                    credentials = RepositoryCredentials("testuser", "testtoken")
                ),
                branch = "main",
                message = "update profile"
            ),
            profileFiles = listOf("README.md", "README_fr.md")
        )
        siteYml.writeText(yamlMapper.writeValueAsString(updatedSite), UTF_8)
        world.projectDir!!.resolve("site").mkdirs()
        world.projectDir!!.resolve("site").resolve("jbake.properties").createNewFile()
        world.projectDir!!.resolve("maquette").mkdirs()
        world.projectDir!!.resolve("maquette").resolve("index.html").createNewFile()
        world.projectDir!!.resolve("README.md").createNewFile()
        world.projectDir!!.resolve("README_fr.md").createNewFile()
    }

    @Given("an existing Bakery project with pushProfile pointing to a simulated remote")
    fun createProjectWithPushProfilePointingToSimulatedRemote() {
        world.createGradleProject()

        // 1. Create bare simulated remote
        val remoteDir = Files.createTempDirectory("simulated-remote-").toFile()
        remoteDir.mkdirs()
        val bareGit = Git.init().setBare(true).setDirectory(remoteDir).call()
        bareGit.close()
        world.simulatedRemoteDir = remoteDir
        world.simulatedRemoteUri = remoteDir.toURI().toString()

        // 2. Update site.yml to point to the simulated remote
        val siteYml = world.projectDir!!.resolve("site.yml")
        val site = yamlMapper.readValue(siteYml, SiteConfiguration::class.java)
        val updatedSite = site.copy(
            pushProfile = GitPushConfiguration(
                from = "",
                to = "profile-cvs",
                repo = RepositoryConfiguration(
                    name = "testremote",
                    repository = world.simulatedRemoteUri!!,
                    credentials = RepositoryCredentials("", "")
                ),
                branch = "main",
                message = "update profile"
            ),
            profileFiles = listOf("README.md", "README_fr.md")
        )
        siteYml.writeText(yamlMapper.writeValueAsString(updatedSite), UTF_8)

        // 3. Create required directories so initSite doesn't trigger
        world.projectDir!!.resolve("site").mkdirs()
        world.projectDir!!.resolve("site").resolve("jbake.properties").createNewFile()
        world.projectDir!!.resolve("maquette").mkdirs()
        world.projectDir!!.resolve("maquette").resolve("index.html").createNewFile()
    }

    @And("the simulated remote has a file {string} with content {string}")
    fun createFileInSimulatedRemote(fileName: String, content: String) {
        val remoteUri = world.simulatedRemoteUri
            ?: throw IllegalStateException("Simulated remote URI not set. Ensure the 'an existing Bakery project with pushProfile pointing to a simulated remote' step was executed first.")

        val tempDir = Files.createTempDirectory("simulated-remote-clone-").toFile()
        val cloneGit = Git.cloneRepository()
            .setURI(remoteUri)
            .setDirectory(tempDir)
            .call()
        try {
            tempDir.resolve(fileName).writeText(content, UTF_8)
            cloneGit.add().addFilepattern(fileName).call()
            cloneGit.commit().setMessage("initial commit").call()
            cloneGit.push().setRemote("origin").setRefSpecs(RefSpec("+refs/heads/main:refs/heads/main")).call()
        } finally {
            cloneGit.close()
            tempDir.deleteRecursively()
        }
    }

    @And("the project has profile files:")
    fun createProfileFilesInProject(table: DataTable) {
        val fileNames = table.asList()
        val projectDir = world.projectDir!!
        fileNames.forEach { fileName ->
            projectDir.resolve(fileName).writeText("# $fileName\nContent for $fileName", UTF_8)
        }
    }

    @When("I execute the publishProfile task with credentials {string} and {string}")
    fun executePublishProfileTask(username: String, token: String) = runBlocking {
        val result = create()
            .withProjectDir(world.projectDir!!)
            .withPluginClasspath()
            .withArguments(
                "publishProfile",
                "-PprofileUsername=$username",
                "-PprofileToken=$token",
                "--stacktrace"
            )
            .build()
        world.buildResult = result
    }

    @Then("the simulated remote should contain {string}")
    fun checkSimulatedRemoteContains(fileName: String) {
        val remoteUri = world.simulatedRemoteUri
            ?: throw IllegalStateException("Simulated remote URI not set.")
        val tempDir = Files.createTempDirectory("simulated-remote-verify-").toFile()
        val cloneGit = Git.cloneRepository()
            .setURI(remoteUri)
            .setDirectory(tempDir)
            .call()
        try {
            assertThat(tempDir.resolve(fileName))
                .describedAs("Remote should contain file $fileName")
                .exists()
        } finally {
            cloneGit.close()
            tempDir.deleteRecursively()
        }
    }

    @Then("the simulated remote should still contain {string} with content {string}")
    fun checkSimulatedRemoteContainsWithContent(fileName: String, expectedContent: String) {
        val remoteUri = world.simulatedRemoteUri
            ?: throw IllegalStateException("Simulated remote URI not set.")
        val tempDir = Files.createTempDirectory("simulated-remote-verify-").toFile()
        val cloneGit = Git.cloneRepository()
            .setURI(remoteUri)
            .setDirectory(tempDir)
            .call()
        try {
            assertThat(tempDir.resolve(fileName))
                .describedAs("Remote should still contain $fileName")
                .exists()
                .hasContent(expectedContent)
        } finally {
            cloneGit.close()
            tempDir.deleteRecursively()
        }
    }

    @And("the output of the task {string} does not contain {string}")
    fun checkTaskNotRegistered(tasksTaskName: String, taskName: String): Unit = runBlocking {
        world.executeGradle(tasksTaskName).output
            .run(::assertThat)
            .describedAs("$taskName task should not be registered.")
            .doesNotContain(taskName)
    }
}