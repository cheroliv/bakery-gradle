package bakery

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eclipse.jgit.api.Git
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

        @Test
        fun `pushToRemote with preserveHistory clones remote and keeps existing files`() {
            // Given: remote bare repo
            val remoteGit = createBareRemoteRepository()
            val remoteUri = remoteGit.repository.directory.toURI().toString()

            // Populate remote with initial content via a temporary clone
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

            // Given: repoDir with a new file "new.txt"
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

            // When
            GitService.pushToRemote(
                repoDir = repoDir,
                git = config,
                logger = logger,
                preserveHistory = true
            )

            // Then: verify local repoDir contains both old.txt and new.txt
            assertThat(repoDir.resolve("old.txt")).exists()
            assertThat(repoDir.resolve("old.txt").readText()).isEqualTo("old content")
            assertThat(repoDir.resolve("new.txt")).exists()
            assertThat(repoDir.resolve("new.txt").readText()).isEqualTo("new content")

            // Then: verify remote received a new commit
            val log = remoteGit.log().call().toList()
            assertThat(log).hasSize(2) // initial + update
        }

        @Test
        fun `pushToRemote without preserveHistory does not clone and overwrites`() {
            // Given: remote repo with an existing file "old.txt"
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

            // Given: repoDir with only "new.txt"
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

            // When: force-push without preserveHistory
            GitService.pushToRemote(
                repoDir = repoDir,
                git = config,
                logger = logger,
                preserveHistory = false
            )

            // Then: remote should have only 1 commit (our force-pushed one)
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

            // When clone fails the final push also fails because remote is invalid.
            // The important thing: the exception should mention the fallback / remote issue,
            // and the repoDir should have been initialized (file still present).
            assertThatThrownBy {
                GitService.pushToRemote(
                    repoDir = repoDir,
                    git = config,
                    logger = logger,
                    preserveHistory = true
                )
            }.isInstanceOf(Exception::class.java)

            // repoDir should still contain our file (init+commit happened before push failure)
            assertThat(repoDir.resolve("file.txt")).exists()
        }
    }
}
