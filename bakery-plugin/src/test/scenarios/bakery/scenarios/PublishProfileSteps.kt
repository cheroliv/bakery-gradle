package bakery.scenarios

import bakery.FileSystemManager.yamlMapper
import bakery.GitPushConfiguration
import bakery.RepositoryConfiguration
import bakery.RepositoryCredentials
import bakery.SiteConfiguration
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import kotlin.text.Charsets.UTF_8

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

    @And("the output of the task {string} does not contain {string}")
    fun checkTaskNotRegistered(tasksTaskName: String, taskName: String): Unit = runBlocking {
        world.executeGradle(tasksTaskName).output
            .run(::assertThat)
            .describedAs("$taskName task should not be registered.")
            .doesNotContain(taskName)
    }
}