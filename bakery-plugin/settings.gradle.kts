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

plugins { this.id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

rootProject.name = "bakery-plugin"
include("bakery")
