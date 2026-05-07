package bakery.scenarios

import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import org.assertj.core.api.Assertions.assertThat

class ConfigureSiteSteps(private val world: BakeryWorld) {

    @And("the project has the bake and maquette directories ready")
    fun projectHasBakeAndMaquetteDirectoriesReady() {
        val dir = world.projectDir ?: throw IllegalStateException("Project dir not initialized")
        dir.resolve("site").mkdirs()
        dir.resolve("site/jbake.properties").apply {
            createNewFile()
            writeText("site.host=https://example.com/\nbake.srcPath=site\nbake.destDirPath=bake\n")
        }
        dir.resolve("site/content").mkdirs()
        dir.resolve("site/templates").mkdirs()
        dir.resolve("site/assets").mkdirs()
        dir.resolve("maquette").mkdirs()
        dir.resolve("maquette/index.html").apply {
            createNewFile()
            writeText("<html></html>")
        }
    }

    @Then("the output should contain configureSite task information")
    fun outputShouldContainConfigureSiteTaskInfo() {
        val output = world.buildResult?.output ?: ""

        assertThat(output)
            .describedAs("tasks output should contain configureSite")
            .contains("configureSite")
        assertThat(output)
            .describedAs("tasks output should contain Bakery group and configureSite description")
            .contains("Bakery tasks", "Initialize Bakery configuration.")
    }
}
