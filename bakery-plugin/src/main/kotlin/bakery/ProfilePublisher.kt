package bakery

import org.slf4j.Logger
import java.io.File

object ProfilePublisher {

    fun resolveCredentials(
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

    fun copyProfileFiles(
        profileFiles: List<String>,
        projectDir: File,
        from: String,
        repoDir: File,
        logger: Logger
    ) {
        if (profileFiles.isEmpty()) {
            throw IllegalStateException("No profile files specified in site.yml (profileFiles)")
        }

        val fromDir = if (from.isBlank()) projectDir else projectDir.resolve(from)
        if (!fromDir.exists()) {
            throw IllegalStateException("Profile from directory does not exist: ${fromDir.absolutePath}")
        }
        if (!fromDir.isDirectory) {
            throw IllegalStateException("Profile from path is not a directory: ${fromDir.absolutePath}")
        }

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
    from = this.from,
    to = this.to,
    repo = RepositoryConfiguration(
        name = this.repo.name,
        repository = this.repo.repository,
        credentials = RepositoryCredentials(username, password)
    ),
    branch = this.branch,
    message = this.message
)
