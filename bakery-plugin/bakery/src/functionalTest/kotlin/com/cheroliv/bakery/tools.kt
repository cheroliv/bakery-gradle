package com.cheroliv.bakery

import org.assertj.core.api.Assertions.assertThat
import java.io.File
import kotlin.text.Charsets.UTF_8


fun File.createBuildScriptFile() {
    resolve("settings.gradle.kts").run {
        assertThat(exists())
            .describedAs("settings.gradle.kts should not exists yet.")
            .isFalse
        assertThat(createNewFile())
            .describedAs("setting.gradle.kts should be created.")
            .isTrue
        writeText(
            """
                plugins {
                    alias(libs.plugins.kotlin.jvm)
                    alias(libs.plugins.bakery)
                }

                bakery { configPath = file("site.yml").absolutePath }
            """.trimIndent(), UTF_8
        )

    }
}

fun File.createSettingsFile() {
    resolve("settings.gradle.kts").run {
        assertThat(exists())
            .describedAs("settings.gradle.kts should not exists yet.")
            .isFalse
        assertThat(createNewFile())
            .describedAs("setting.gradle.kts should be created.")
            .isTrue
        writeText(
            """
            @file:Suppress("UnstableApiUsage")

            pluginManagement {
                repositories {
                    mavenLocal()
                    gradlePluginPortal()
                    mavenCentral()
                    google()
                }
            }

            dependencyResolutionManagement {
                repositories {
                    mavenLocal()
                    mavenCentral()
                    google()
                }
            }

            rootProject.name = "bakery-test"
        """, UTF_8
        )
        assertThat(exists())
            .describedAs("settings.gradle.kts should now exists.")
            .isTrue

        assertThat(readText(UTF_8))
            .describedAs("settings.gradle.kts should contains at least 'bakery-test'")
            .contains("bakery-test")
    }
}
