package bakery

import bakery.FileSystemManager.createRepoDir
import bakery.FileSystemManager.from
import bakery.GitService.cleanupDir
import bakery.GitService.pushToRemote
import bakery.ProfilePublisher.copyProfileFiles
import bakery.ProfilePublisher.resolveCredentials
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger

abstract class PublishProfileTask : DefaultTask() {

    @TaskAction
    fun publishProfile() {
        val profileToken: String = project.findProperty("profileToken")?.toString() ?: ""
        val profileUsername: String = project.findProperty("profileUsername")?.toString() ?: ""

        val configPath = project.extensions.findByType(BakeryExtension::class.java)
            ?.configPath?.orNull ?: "site.yml"
        val site = project.from(configPath)
        val pushProfile = site.pushProfile
            ?: throw IllegalStateException("pushProfile section not found in site.yml")

        val credentials: Pair<String, String> = resolveCredentials(pushProfile, profileUsername, profileToken)
        val gitConfig = pushProfile.withCredentials(credentials.first, credentials.second)

        val buildDir = project.layout.buildDirectory.get().asFile
        val repoDir = createRepoDir("${buildDir.absolutePath}/${pushProfile.to}", project.logger)

        try {
            copyProfileFiles(site.profileFiles, project.projectDir, pushProfile.from, repoDir, project.logger)
            pushToRemote(repoDir, gitConfig, project.logger, preserveHistory = true)
        } finally {
            cleanupDir(repoDir, project.logger)
        }
    }
}
