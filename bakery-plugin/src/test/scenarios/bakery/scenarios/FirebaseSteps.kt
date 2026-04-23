package bakery.scenarios

import io.cucumber.java.en.Then
import org.assertj.core.api.Assertions.assertThat
import kotlin.text.Charsets.UTF_8

class FirebaseSteps(private val world: BakeryWorld) {

    @Then("the {string} file in {string} directory should contain {string} and {string}")
    fun jbakePropertiesShouldContainFirebaseKeys(
        fileName: String,
        dirName: String,
        key1: String,
        key2: String
    ) {
        world.projectDir!!
            .resolve(dirName)
            .resolve(fileName).apply {
                run(::assertThat)
                    .describedAs("the $dirName directory should contain $fileName file")
                    .exists()
                    .isFile
            }.readText(UTF_8)
            .run(::assertThat)
            .describedAs("$fileName should contain $key1 and $key2")
            .contains(key1, key2)
    }
}