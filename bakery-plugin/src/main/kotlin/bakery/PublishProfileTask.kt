package bakery

import bakery.FileSystemManager.createRepoDir
import bakery.FileSystemManager.from
import bakery.GitService.cleanupDir
import bakery.GitService.pushToRemote
import org.gradle.api.DefaultTask

import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import java.io.File

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

    private fun resolveCredentials(
        git: GitPushConfiguration,
        cliUsername: String,
        cliPassword: String
    ): Pair<String, String> {
        val username = cliUsername.ifBlank { git.repo.credentials.username }
        val password = cliPassword.ifBlank { git.repo.credentials.password }
        if (username.isBlank() || password.isBlank()) {
            throw IllegalStateException(
                "Profile credentials not found. Use -PprofileUsername=<user> -PprofileToken=<token> " +
                    "or set credentials in site.yml under pushProfile.repo.credentials"
            )
        }
        return Pair(username, password)
    }

    private fun copyProfileFiles(
        profileFiles: List<String>,
        projectDir: File,
        from: String,
        repoDir: File,
        logger: Logger
    ) {
        if (profileFiles.isEmpty()) {
            throw IllegalStateException("No profile files specified in site.yml (profileFiles)")
        }
        // Build the effective source directory
        val fromDir = if (from.isBlank()) projectDir else projectDir.resolve(from)
        if (!fromDir.exists()) {
            throw IllegalStateException("Profile from directory does not exist: ${fromDir.absolutePath}")
        }
        if (!fromDir.isDirectory) {
            throw IllegalStateException("Profile from path is not a directory: ${fromDir.absolutePath}")
        }

        // Gilet-par-balle: interdire les noms de fichiers connus du plugin
        val forbiddenFiles = listOf("README.adoc", "README_fr.adoc")
        val intersect = profileFiles.toSet().intersect(forbiddenFiles.toSet())
        if (intersect.isNotEmpty()) {
            throw IllegalStateException(
                "Forbidden profile files detected from plugin directory: $intersect. " +
                "Ensure publishProfile is executed from the consumer project, not the plugin itself."
            )
        }

        profileFiles.forEach { fileName ->
            val source = fromDir.resolve(fileName)
            if (!source.exists()) {
                throw IllegalStateException("Profile file not found: ${source.absolutePath}")
            }
            val target = repoDir.resolve(fileName)
            source.copyTo(target, overwrite = true)
            logger.info("Copied profile file: ${source.absolutePath} -> ${target.absolutePath}")
        }
    }
}

fun GitPushConfiguration.withCredentials(
    username: String,
    password: String
): GitPushConfiguration = GitPushConfiguration(
    from = from,
    to = to,
    repo = RepositoryConfiguration(
        name = repo.name,
        repository = repo.repository,
        credentials = RepositoryCredentials(username, password)
    ),
    branch = branch,
    message = message
)