package com.cheroliv.bakery

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.Console
import java.lang.System.console
import java.lang.System.getenv

object ConfigPrompts {

    fun Project.getOrPrompt(
        propertyName: String,
        cliProperty: String,
        sensitive: Boolean = false,
        example: String? = null,
        default: String? = null
    ): String {
        // 1. Vérifier les propriétés du projet (-P)
        if (hasProperty(cliProperty)) {
            val value = property(cliProperty) as String
            if (value.isNotBlank()) return value
        }

        // 2. Vérifier les variables d'environnement
        val envVar = cliProperty.uppercase().replace(Regex("([a-z])([A-Z])"), "$1_$2")
        getenv(envVar)?.takeIf { it.isNotBlank() }?.let { return it }

        // 3. Utiliser la valeur par défaut si fournie
        default?.let { return it }

        // 4. Demander interactivement
        //return promptUser(propertyName, sensitive, example, project.logger)
        console().let {
            return if (it != null) {
                if (sensitive) promptSensitive(it, propertyName, logger)
                else promptNormal(it, propertyName, example, logger)
            } else promptFallback(propertyName, sensitive, example, logger)
        }
    }

    private fun promptSensitive(
        console: Console,
        propertyName: String,
        logger: Logger
    ): String {
        var input: CharArray?
        do {
            print("Enter $propertyName (hidden): ")
            input = console.readPassword()
            if (input == null || input.isEmpty())
                logger.warn("$propertyName cannot be empty. Please try again.")
        } while (input == null || input.isEmpty())

        return String(input).also {
            input.fill('0')
        }
    }

    private fun promptNormal(
        console: Console,
        propertyName: String,
        example: String?,
        logger: Logger
    ): String {
        val exampleText = example?.let { " (e.g., $it)" } ?: ""
        var input: String?
        do {
            print("Enter $propertyName$exampleText: ")
            input = console.readLine()
            if (input.isNullOrBlank())
                logger.warn("$propertyName cannot be empty. Please try again.")
        } while (input.isNullOrBlank())
        return input
    }

    private fun promptFallback(
        propertyName: String,
        sensitive: Boolean,
        example: String?,
        logger: Logger
    ): String {
        val exampleText = example?.let { " (e.g., $it)" } ?: ""
        val sensitiveNote = if (sensitive) " (will be visible)" else ""

        logger.lifecycle("Console not available. Using standard input.")
        print("Enter $propertyName$exampleText$sensitiveNote: ")

        var input: String?

        do {
            input = readlnOrNull()
            if (input.isNullOrBlank()) {
                logger.warn("$propertyName cannot be empty. Please try again.")
                print("Enter $propertyName$exampleText: ")
            }
        } while (input.isNullOrBlank())

        return input
    }

    fun Project.saveConfiguration(
        site: SiteConfiguration,
        isGradlePropertiesEnabled: Boolean,
    ) {
//        //TODO: changer ca en sauvegarder ou update site.yml
//        // Sauvegarder les credentials GitHub
//        val githubConfigFile = rootProject.file(".github-config")
//        githubConfigFile.writeText(
//            """
//            |# GitHub Configuration
//            |# DO NOT COMMIT THIS FILE - Add it to .gitignore
//            |github.username=$username
//            |github.repo=$repo
//            |github.token=$token
//        """.trimMargin()
//        )
//        // Sauvegarder le chemin de configuration dans gradle.properties
//        val gradlePropertiesFile = rootProject.file("gradle.properties")
//        val properties = if (gradlePropertiesFile.exists())
//            gradlePropertiesFile.readLines().toMutableList()
//        else mutableListOf()
//
//        // Retirer l'ancienne ligne configPath si elle existe
//        properties.removeIf { it.startsWith("$BAKERY_CONFIG_PATH_KEY=") }
//        properties.add("$BAKERY_CONFIG_PATH_KEY=$configPath")
//
//        gradlePropertiesFile.writeText(properties.joinToString("\n"))
//
//        logger.lifecycle("Configuration saved to:")
//        logger.lifecycle("  - ${githubConfigFile.absolutePath}")
//        logger.lifecycle("  - ${gradlePropertiesFile.absolutePath}")
//        logger.warn("")
//        logger.warn("⚠️  IMPORTANT SECURITY NOTES:")
//        logger.warn("  • Add .github-config to your .gitignore")
//        logger.warn("  • Never commit your GitHub token")
//
//        // Vérifier .gitignore
//        rootProject.file(".gitignore").run {
//            if (exists()) {
//                if (!readText()
//                        .contains(".github-config")
//                ) logger.warn("  • .github-config is NOT in your .gitignore!")
//            } else logger.warn("  • No .gitignore file found!")
//        }
    }
}