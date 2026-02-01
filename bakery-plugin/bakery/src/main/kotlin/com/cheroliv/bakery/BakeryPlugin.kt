package com.cheroliv.bakery

import com.cheroliv.bakery.FileSystemManager.from
import com.cheroliv.bakery.SiteManager.BAKERY_GROUP
import com.cheroliv.bakery.SiteManager.configureBakeTask
import com.cheroliv.bakery.SiteManager.configureConfigPath
import com.cheroliv.bakery.SiteManager.configureJBakePlugin
import com.cheroliv.bakery.SiteManager.createJBakeRuntimeConfiguration
import com.cheroliv.bakery.SiteManager.registerConfigureSiteTask
import com.cheroliv.bakery.SiteManager.registerInitSiteTask
import com.cheroliv.bakery.SiteManager.registerPublishMaquetteTask
import com.cheroliv.bakery.SiteManager.registerPublishSiteTask
import com.cheroliv.bakery.SiteManager.registerServeTask
import com.cheroliv.bakery.SiteManager.registerUtilityTasks
import org.gradle.api.Plugin
import org.gradle.api.Project


class BakeryPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val jbakeRuntime = project.createJBakeRuntimeConfiguration()
        val bakeryExtension = project.extensions.create(
            BAKERY_GROUP,
            BakeryExtension::class.java
        )
        val isGradlePropertiesEnabled = bakeryExtension.configPath.isPresent

        project.afterEvaluate {
            project.configureConfigPath(bakeryExtension, isGradlePropertiesEnabled)
            val configFile = project.layout
                .projectDirectory.asFile
                .resolve(bakeryExtension.configPath.get())

            if (!configFile.exists()) {
                "config file does not exists."
                    .apply(::println)
                    .let(project.logger::info)
                project.registerInitSiteTask()
            } else {
                val site = project.from(bakeryExtension.configPath.get())
                project.configureJBakePlugin(site)
                project.configureBakeTask(site)
                project.registerPublishSiteTask(site)
                project.registerPublishMaquetteTask(site)
                project.registerServeTask(site, jbakeRuntime)
                project.registerUtilityTasks()
                project.registerConfigureSiteTask(site, isGradlePropertiesEnabled)
            }
        }
    }
}