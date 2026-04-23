package bakery

import bakery.FileSystemManager.from
import bakery.FileSystemManager.yamlMapper
import bakery.SiteManager.BAKERY_GROUP
import bakery.SiteManager.configureBakeTask
import bakery.SiteManager.configureConfigPath
import bakery.SiteManager.configureJBakePlugin
import bakery.SiteManager.createJBakeRuntimeConfiguration
import bakery.SiteManager.registerConfigureSiteTask
import bakery.SiteManager.registerInitSiteTask
import bakery.SiteManager.registerPublishMaquetteTask
import bakery.SiteManager.registerPublishSiteTask
import bakery.SiteManager.registerServeTask
import bakery.SiteManager.registerUtilityTasks
import com.fasterxml.jackson.module.kotlin.readValue
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
            if (!configFile.exists() || (configFile.exists() &&
                        yamlMapper.readValue<SiteConfiguration>(configFile).run {
                            !project.projectDir.resolve(bake.srcPath).exists() &&
                                    !project.projectDir.resolve(pushMaquette.from).exists()
                        })
            ) {
                "config file does not exists or site and maquette directories do not exist."
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