package bakery

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.RefSpec
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import org.slf4j.LoggerFactory
import java.io.File

class GitServiceTest {

    private val logger = LoggerFactory.getLogger(GitServiceTest::class.java)

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PushToRemoteHistoryPreservationTest {

        @TempDir
        lateinit var tempDir: File

        private fun createBareRemoteRepository(): Git {
            val remoteDir = tempDir.resolve("remote.git")
            remoteDir.mkdirs()
            return Git.init()
                .setDirectory(remoteDir)
                .setBare(true)
                .call()
        }

        private fun pushHeadToRemote(git: Git, branch: String, remoteName: String = "origin") {
            git.push()
                .setRemote(remoteName)
                .setRefSpecs(RefSpec("+HEAD:refs/heads/$branch"))
                .call()
        }

        private fun commitOnBranch(localClone: Git, fileName: String, content: String, message: String): RevCommit {
            localClone.repository.workTree.resolve(fileName).writeText(content)
            localClone.add().addFilepattern(fileName).call()
            return localClone.commit().setMessage(message).call()
        }

        @Test
        fun `pushToRemote with preserveHistory clones remote and keeps existing files`() {
            val remoteGit = createBareRemoteRepository()
            val remoteUri = remoteGit.repository.directory.toURI().toString()

            val localRemote = tempDir.resolve("localRemote")
            val cloneGit = Git.cloneRepository()
                .setURI(remoteUri)
                .setDirectory(localRemote)
                .call()
            try {
                cloneGit.repository.workTree.resolve("old.txt").writeText("old content")
                cloneGit.add().addFilepattern("old.txt").call()
                cloneGit.commit().setMessage("initial commit").call()
                pushHeadToRemote(cloneGit, "main")
            } finally {
                cloneGit.close()
            }

            val repoDir = tempDir.resolve("repoDir")
            repoDir.mkdirs()
            repoDir.resolve("new.txt").writeText("new content")

            val config = GitPushConfiguration(
                from = "",
                to = "",
                repo = RepositoryConfiguration(
                    name = "test",
                    repository = remoteUri,
                    credentials = RepositoryCredentials("", "")
                ),
                branch = "main",
                message = "update"
            )

            GitService.pushToRemote(
                repoDir = repoDir,
                git = config,
                logger = logger,
                preserveHistory = true
            )

            assertThat(repoDir.resolve("old.txt")).exists()
            assertThat(repoDir.resolve("old.txt").readText()).isEqualTo("old content")
            assertThat(repoDir.resolve("new.txt")).exists()
            assertThat(repoDir.resolve("new.txt").readText()).isEqualTo("new content")

            val log = remoteGit.log().call().toList()
            assertThat(log).hasSize(2)
        }

        @Test
        fun `pushToRemote without preserveHistory does not clone and overwrites`() {
            val remoteGit = createBareRemoteRepository()
            val remoteUri = remoteGit.repository.directory.toURI().toString()

            val localRemote = tempDir.resolve("localRemote2")
            val cloneGit = Git.cloneRepository()
                .setURI(remoteUri)
                .setDirectory(localRemote)
                .call()
            try {
                cloneGit.repository.workTree.resolve("old.txt").writeText("old content")
                cloneGit.add().addFilepattern("old.txt").call()
                cloneGit.commit().setMessage("initial commit").call()
                pushHeadToRemote(cloneGit, "main")
            } finally {
                cloneGit.close()
            }

            val repoDir = tempDir.resolve("repoDir2")
            repoDir.mkdirs()
            repoDir.resolve("new.txt").writeText("new content")

            val config = GitPushConfiguration(
                from = "",
                to = "",
                repo = RepositoryConfiguration(
                    name = "test",
                    repository = remoteUri,
                    credentials = RepositoryCredentials("", "")
                ),
                branch = "main",
                message = "update"
            )

            GitService.pushToRemote(
                repoDir = repoDir,
                git = config,
                logger = logger,
                preserveHistory = false
            )

            val log = remoteGit.log().call().toList()
            assertThat(log).hasSize(1)
        }

        @Test
        fun `pushToRemote with preserveHistory falls back to init if clone fails`() {
            val repoDir = tempDir.resolve("repoDir3")
            repoDir.mkdirs()
            repoDir.resolve("file.txt").writeText("content")

            val config = GitPushConfiguration(
                from = "",
                to = "",
                repo = RepositoryConfiguration(
                    name = "test",
                    repository = "invalid://no-such-repo.git",
                    credentials = RepositoryCredentials("", "")
                ),
                branch = "main",
                message = "update"
            )

            assertThatThrownBy {
                GitService.pushToRemote(
                    repoDir = repoDir,
                    git = config,
                    logger = logger,
                    preserveHistory = true
                )
            }.isInstanceOf(Exception::class.java)

            assertThat(repoDir.resolve("file.txt")).exists()
        }

        @Test
        fun `preserveHistory clones remote even when repoDir is initially empty`() {
            val remoteGit = createBareRemoteRepository()
            val remoteUri = remoteGit.repository.directory.toURI().toString()

            val localRemote = tempDir.resolve("localRemote3")
            val cloneGit = Git.cloneRepository()
                .setURI(remoteUri)
                .setDirectory(localRemote)
                .call()
            try {
                cloneGit.repository.workTree.resolve("remote_only.txt").writeText("remote")
                cloneGit.add().addFilepattern("remote_only.txt").call()
                cloneGit.commit().setMessage("initial commit").call()
                pushHeadToRemote(cloneGit, "main")
            } finally {
                cloneGit.close()
            }

            val repoDir = tempDir.resolve("repoDirEmpty")
            repoDir.mkdirs()

            val config = GitPushConfiguration(
                from = "",
                to = "",
                repo = RepositoryConfiguration(
                    name = "test",
                    repository = remoteUri,
                    credentials = RepositoryCredentials("", "")
                ),
                branch = "main",
                message = "update"
            )

            GitService.pushToRemote(
                repoDir = repoDir,
                git = config,
                logger = logger,
                preserveHistory = true
            )

            assertThat(repoDir.resolve("remote_only.txt")).exists().hasContent("remote")
        }

        @Test
        fun `preserveHistory overlay overwrites existing remote file with local content`() {
            val remoteGit = createBareRemoteRepository()
            val remoteUri = remoteGit.repository.directory.toURI().toString()

            val localRemote = tempDir.resolve("localRemote4")
            val cloneGit = Git.cloneRepository()
                .setURI(remoteUri)
                .setDirectory(localRemote)
                .call()
            try {
                cloneGit.repository.workTree.resolve("shared.txt").writeText("remote_version")
                cloneGit.add().addFilepattern("shared.txt").call()
                cloneGit.commit().setMessage("initial commit").call()
                pushHeadToRemote(cloneGit, "main")
            } finally {
                cloneGit.close()
            }

            val repoDir = tempDir.resolve("repoDirOverlay")
            repoDir.mkdirs()
            repoDir.resolve("shared.txt").writeText("local_version")

            val config = GitPushConfiguration(
                from = "",
                to = "",
                repo = RepositoryConfiguration(
                    name = "test",
                    repository = remoteUri,
                    credentials = RepositoryCredentials("", "")
                ),
                branch = "main",
                message = "update"
            )

            GitService.pushToRemote(
                repoDir = repoDir,
                git = config,
                logger = logger,
                preserveHistory = true
            )

            assertThat(repoDir.resolve("shared.txt")).exists().hasContent("local_version")
        }

        @Test
        fun `preserveHistory works when repoDir contains nested directories`() {
            val remoteGit = createBareRemoteRepository()
            val remoteUri = remoteGit.repository.directory.toURI().toString()

            val localRemote = tempDir.resolve("localRemote5")
            val cloneGit = Git.cloneRepository()
                .setURI(remoteUri)
                .setDirectory(localRemote)
                .call()
            try {
                cloneGit.repository.workTree.resolve("root.txt").writeText("root")
                cloneGit.add().addFilepattern("root.txt").call()
                cloneGit.commit().setMessage("initial commit").call()
                pushHeadToRemote(cloneGit, "main")
            } finally {
                cloneGit.close()
            }

            val repoDir = tempDir.resolve("repoDirNested")
            repoDir.mkdirs()
            val subDir = repoDir.resolve("sub").apply { mkdirs() }
            subDir.resolve("nested.txt").writeText("nested content")

            val config = GitPushConfiguration(
                from = "",
                to = "",
                repo = RepositoryConfiguration(
                    name = "test",
                    repository = remoteUri,
                    credentials = RepositoryCredentials("", "")
                ),
                branch = "main",
                message = "update"
            )

            GitService.pushToRemote(
                repoDir = repoDir,
                git = config,
                logger = logger,
                preserveHistory = true
            )

            assertThat(repoDir.resolve("root.txt")).exists().hasContent("root")
            assertThat(repoDir.resolve("sub/nested.txt")).exists().hasContent("nested content")
        }
    }
}
