@file:Suppress("FunctionName")

package com.cheroliv.bakery

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.GradleException
import org.gradle.testkit.runner.GradleRunner.create
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.text.Charsets.UTF_8

class BakeryPluginInitConfigTaskFunctionalTests {
    companion object {
        private val log: Logger by lazy { getLogger(BakeryPluginInitSiteTaskFunctionalTests::class.java) }

        fun info(message: String) {
            message
                .apply(log::info)
                .run(::println)
        }
    }

    @field:TempDir
    lateinit var projectDir: File

    val File.configFile: File
        get() = if (absolutePath == projectDir.absolutePath)
            resolve("site.yml")
        else projectDir.resolve("site.yml")

    @BeforeTest
    fun prepare() {
        // Do site.yml exist in root project folder?
        assertThat(projectDir.resolve("site.yml"))
            .describedAs("site.yml should not exists yet")
            .doesNotExist()
        info("Prepare temporary directory to host gradle build.")
        projectDir.createSettingsFile()
        projectDir.createBuildScriptFile()
        projectDir.createDependenciesFile()
        projectDir.createConfigFile()
    }

    @Test
    fun `tasks displays with config file`() {
        val result = create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("tasks", "--group=bakery")
            .withProjectDir(projectDir)
            .build()
        assertThat(result.output)
            .describedAs("""Gradle task tasks output should contains 'initConfig' and 'Initialize configuration.'""")
            .contains("Initialize configuration.", "initConfig")
        info("✓ tasks displays the initConfig task's description correctly")
        info(projectDir.configFile.readText(UTF_8))
    }

    @Test
    fun `tasks displays without config file`() {
        projectDir.resolve("site.yml").run {
            when {
                exists() -> when {
                    delete() -> info("site.yml deleted")
                    else -> throw GradleException("site.yml cannot be deleted")
                }
            }
        }
        info("site.yml file successfully deleted.")
        assertThrows<UnexpectedBuildFailure> {
            create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("tasks", "--group=bakery")
                .withProjectDir(projectDir)
                .build()
        }
        info("✓ without config file, the project fails to build.")
    }
}