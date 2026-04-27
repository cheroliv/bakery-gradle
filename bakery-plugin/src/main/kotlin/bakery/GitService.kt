package bakery

import bakery.FileSystemManager.copyBakedFilesToRepo
import bakery.FileSystemManager.createRepoDir
import bakery.GitService.FileOperationResult.Failure
import bakery.GitService.FileOperationResult.Success
import bakery.RepositoryConfiguration.Companion.ORIGIN
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.Logger
import java.io.File
import java.io.IOException

object GitService {
    const val GIT_ATTRIBUTES_CONTENT = """
    #
    # https://help.github.com/articles/dealing-with-line-endings/
    #
    # Linux start script should use lf
    /gradlew        text eol=lf

    # These are Windows script files and should use crlf
    *.bat           text eol=crlf

    # Binary files should be left untouched
    *.jar           binary
    """

    sealed class FileOperationResult {
        sealed class GitOperationResult {
            data class Success(
                val commit: RevCommit, val pushResults: MutableIterable<PushResult>?
            ) : GitOperationResult()

            data class Failure(val error: String) : GitOperationResult()
        }

        object Success : FileOperationResult()
        data class Failure(val error: String) : FileOperationResult()
    }

    fun pushPages(
        destPath: () -> String,
        pathTo: () -> String,
        git: GitPushConfiguration,
        logger: Logger
    ) {
        val repoDir: File = createRepoDir(pathTo(), logger)
        try {
            when (val copyResult = copyBakedFilesToRepo(destPath(), repoDir, logger)) {
                is Success -> {
                    logger.info("Successfully copied files to publication repository.")
                    pushToRemote(repoDir, git, logger)
                }

                is Failure -> {
                    logger.error("Failed to copy baked files: ${copyResult.error}")
                    throw Exception("Publication failed during file copy: ${copyResult.error}")
                }
            }
        } finally {
            cleanupPublicationArtifacts(repoDir, destPath(), logger)
        }
    }

    fun pushToRemote(
        repoDir: File,
        git: GitPushConfiguration,
        logger: Logger,
        force: Boolean = true,
        preserveHistory: Boolean = false
    ) {
        logger.info("Starting Git operations.")
        var effectiveForce = force
        if (preserveHistory && git.repo.repository.isNotBlank()) {
            try {
                cloneAndOverlay(repoDir, git, logger)
                effectiveForce = false // pas besoin de force quand on a cloné
            } catch (e: Exception) {
                logger.warn(
                    "Failed to clone remote for history preservation: ${e.message}. " +
                        "Falling back to init+force.",
                    e
                )
            }
        }
        initAddCommit(repoDir, git, logger)
        executePushCommand(
            openRepository(repoDir, logger),
            git,
            logger,
            effectiveForce
        )?.forEach { pushResult ->
            val resultString = pushResult.toString()
            logger.info(resultString)
            println(resultString)
        }
        logger.info("Git push completed.")
    }

    private fun cloneAndOverlay(
        repoDir: File,
        git: GitPushConfiguration,
        logger: Logger
    ) {
        val tempCloneDir = File(repoDir.parentFile, "${repoDir.name}.bakeryclone")
        try {
            Git.cloneRepository()
                .setURI(git.repo.repository)
                .setDirectory(tempCloneDir)
                .setBranch(git.branch)
                .call()
            logger.info("Cloned remote repository to ${tempCloneDir.absolutePath}")

            if (repoDir.exists() && repoDir.isDirectory) {
                repoDir.walkTopDown().filter { it.isFile }.forEach { source ->
                    val relative = source.relativeTo(repoDir)
                    val target = tempCloneDir.resolve(relative)
                    target.parentFile.mkdirs()
                    source.copyTo(target, overwrite = true)
                }
                logger.info("Overlayed local files onto cloned repository.")
            }

            if (!repoDir.deleteRecursively()) {
                throw IOException("Failed to delete ${repoDir.absolutePath}")
            }
            if (!tempCloneDir.renameTo(repoDir)) {
                throw IOException(
                    "Failed to rename ${tempCloneDir.absolutePath} to ${repoDir.absolutePath}"
                )
            }
            logger.info("Replaced repoDir with cloned repository.")
        } catch (e: Exception) {
            if (tempCloneDir.exists()) tempCloneDir.deleteRecursively()
            throw e
        }
    }

    fun cleanupDir(dir: File, logger: Logger) {
        logger.info("Cleaning up directory: ${dir.absolutePath}")
        try {
            if (dir.exists()) {
                dir.deleteRecursively()
                logger.info("Deleted directory: $dir")
            }
        } catch (e: Exception) {
            logger.error("Error during cleanup: ${e.message}", e)
        }
    }

    private fun cleanupPublicationArtifacts(
        repoDir: File,
        destPath: String,
        logger: Logger
    ) {
        logger.info("Cleaning up publication artifacts.")
        try {
            if (repoDir.exists()) {
                repoDir.deleteRecursively()
                logger.info("Deleted temporary repository directory: $repoDir")
            }
            val destDir = File(destPath)
            if (destDir.exists()) {
                destDir.deleteRecursively()
                logger.info("Deleted baked output directory: $destDir")
            }
        } catch (e: Exception) {
            logger.error("Error during cleanup: ${e.message}", e)
        }
    }

    private fun openRepository(repoDir: File, logger: Logger): Git {
        logger.info("Opening repository at: ${repoDir.absolutePath}")
        val repository = FileRepositoryBuilder().setGitDir(File(repoDir, ".git"))
            .readEnvironment()
            .findGitDir()
            .setMustExist(true)
            .build()

        if (repository.isBare) {
            val errorMessage = "$repository must not be bare."
            logger.error(errorMessage)
            throw IllegalStateException(errorMessage)
        }
        logger.info("Repository opened successfully.")
        return Git(repository)
    }

    private fun executePushCommand(
        git: Git,
        gitConfig: GitPushConfiguration,
        logger: Logger,
        force: Boolean = true
    ): MutableIterable<PushResult>? {
        logger.info("Preparing to push to remote '$ORIGIN' on branch '${gitConfig.branch}' (force=$force)")
        val credentialsProvider = UsernamePasswordCredentialsProvider(
            gitConfig.repo.credentials.username,
            gitConfig.repo.credentials.password
        )

        return git.push().apply {
            setCredentialsProvider(credentialsProvider)
            remote = ORIGIN
            isForce = force
        }.call()
    }

    fun initAddCommit(
        repoDir: File,
        git: GitPushConfiguration,
        logger: Logger
    ): RevCommit = initRepository(repoDir, git.branch, logger)
        .addRemote(git.repo.repository, logger)
        .addAllFiles(logger)
        .commitChanges(git.message, logger)

    private fun initRepository(
        repoDir: File,
        branch: String,
        logger: Logger
    ): Git {
        logger.info("Initializing repository in $repoDir on branch $branch")
        val git = Git.init()
            .setInitialBranch(branch)
            .setDirectory(repoDir)
            .call()
        if (git.repository.isBare)
            throw Exception("Repository must not be bare")
        if (!git.repository.directory.isDirectory)
            throw Exception("Repository path must be a directory")
        logger.info("Repository initialized successfully.")
        return git
    }

    private fun Git.addRemote(
        remoteUri: String,
        logger: Logger
    ): Git {
        logger.info("Adding remote '$ORIGIN' with URI '$remoteUri'")
        remoteAdd()
            .setName(ORIGIN)
            .setUri(URIish(remoteUri))
            .call()
        logger.info("Remote added successfully.")
        return this
    }

    private fun Git.addAllFiles(logger: Logger): Git {
        logger.info("Adding all files to the index")
        add()
            .addFilepattern(".")
            .call()
        logger.info("All files added.")
        return this
    }

    private fun Git.commitChanges(
        message: String,
        logger: Logger
    ): RevCommit {
        logger.info("Committing changes with message: \"$message\"")
        val revCommit = commit()
            .setMessage(message)
            .call()
        logger.info("Changes committed: ${revCommit.id.name}")
        return revCommit
    }
}