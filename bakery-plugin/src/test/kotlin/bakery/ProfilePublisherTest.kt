package bakery

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import org.slf4j.LoggerFactory
import java.io.File

class ProfilePublisherTest {

    private val logger = LoggerFactory.getLogger(ProfilePublisherTest::class.java)

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ResolveCredentialsTest {

        private fun gitConfig(
            user: String = "configUser",
            pass: String = "configPass"
        ) = GitPushConfiguration(
            from = "",
            to = "",
            repo = RepositoryConfiguration(
                name = "test",
                repository = "https://example.com/test.git",
                credentials = RepositoryCredentials(user, pass)
            ),
            branch = "main",
            message = "msg"
        )

        @Test
        fun `returns CLI credentials when both provided`() {
            val result = ProfilePublisher.resolveCredentials(gitConfig(), "cliUser", "cliPass")
            assertThat(result.first).isEqualTo("cliUser")
            assertThat(result.second).isEqualTo("cliPass")
        }

        @Test
        fun `falls back to config credentials when CLI is blank`() {
            val result = ProfilePublisher.resolveCredentials(gitConfig(), "", "")
            assertThat(result.first).isEqualTo("configUser")
            assertThat(result.second).isEqualTo("configPass")
        }

        @Test
        fun `mixes CLI username and config password`() {
            val result = ProfilePublisher.resolveCredentials(
                gitConfig(user = "", pass = "configPass"),
                "cliUser",
                ""
            )
            assertThat(result.first).isEqualTo("cliUser")
            assertThat(result.second).isEqualTo("configPass")
        }

        @Test
        fun `throws when both sources are blank`() {
            val emptyConfig = gitConfig(user = "", pass = "")
            assertThatThrownBy {
                ProfilePublisher.resolveCredentials(emptyConfig, "", "")
            }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("credentials not found")
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CopyProfileFilesTest {

        @TempDir
        lateinit var tempDir: File

        private fun createRepoDir() = tempDir.resolve("repo").apply { mkdirs() }

        @Test
        fun `copies files from projectDir when from is blank`() {
            val projectDir = tempDir.resolve("project").apply { mkdirs() }
            projectDir.resolve("README.md").writeText("Hello")
            val repoDir = createRepoDir()

            ProfilePublisher.copyProfileFiles(
                profileFiles = listOf("README.md"),
                projectDir = projectDir,
                from = "",
                repoDir = repoDir,
                logger = logger
            )

            assertThat(repoDir.resolve("README.md")).exists().hasContent("Hello")
        }

        @Test
        fun `copies files from subdir when from is set`() {
            val projectDir = tempDir.resolve("project").apply { mkdirs() }
            val subDir = projectDir.resolve("docs").apply { mkdirs() }
            subDir.resolve("README.md").writeText("From docs")
            val repoDir = createRepoDir()

            ProfilePublisher.copyProfileFiles(
                profileFiles = listOf("README.md"),
                projectDir = projectDir,
                from = "docs",
                repoDir = repoDir,
                logger = logger
            )

            assertThat(repoDir.resolve("README.md")).exists().hasContent("From docs")
        }

        @Test
        fun `throws when profileFiles is empty`() {
            assertThatThrownBy {
                ProfilePublisher.copyProfileFiles(
                    profileFiles = emptyList(),
                    projectDir = tempDir,
                    from = "",
                    repoDir = createRepoDir(),
                    logger = logger
                )
            }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("No profile files specified")
        }

        @Test
        fun `throws when from directory does not exist`() {
            assertThatThrownBy {
                ProfilePublisher.copyProfileFiles(
                    profileFiles = listOf("README.md"),
                    projectDir = tempDir,
                    from = "nonexistent",
                    repoDir = createRepoDir(),
                    logger = logger
                )
            }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("does not exist")
        }

        @Test
        fun `throws when from path is a file not directory`() {
            val projectDir = tempDir.resolve("project").apply { mkdirs() }
            projectDir.resolve("notadir").writeText("oops")

            assertThatThrownBy {
                ProfilePublisher.copyProfileFiles(
                    profileFiles = listOf("README.md"),
                    projectDir = projectDir,
                    from = "notadir",
                    repoDir = createRepoDir(),
                    logger = logger
                )
            }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("not a directory")
        }

        @Test
        fun `throws when forbidden files are in profileFiles`() {
            assertThatThrownBy {
                ProfilePublisher.copyProfileFiles(
                    profileFiles = listOf("README.adoc"),
                    projectDir = tempDir,
                    from = "",
                    repoDir = createRepoDir(),
                    logger = logger
                )
            }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("Forbidden profile files")
        }

        @Test
        fun `throws when profile file is not found on disk`() {
            val projectDir = tempDir.resolve("project").apply { mkdirs() }
            assertThatThrownBy {
                ProfilePublisher.copyProfileFiles(
                    profileFiles = listOf("MISSING.md"),
                    projectDir = projectDir,
                    from = "",
                    repoDir = createRepoDir(),
                    logger = logger
                )
            }.isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("Profile file not found")
        }

        @Test
        fun `copies multiple files successfully`() {
            val projectDir = tempDir.resolve("project").apply { mkdirs() }
            projectDir.resolve("README.md").writeText("A")
            projectDir.resolve("README_fr.md").writeText("B")
            val repoDir = createRepoDir()

            ProfilePublisher.copyProfileFiles(
                profileFiles = listOf("README.md", "README_fr.md"),
                projectDir = projectDir,
                from = "",
                repoDir = repoDir,
                logger = logger
            )

            assertThat(repoDir.resolve("README.md")).exists().hasContent("A")
            assertThat(repoDir.resolve("README_fr.md")).exists().hasContent("B")
        }

        @Test
        fun `overwrites existing file in repoDir`() {
            val projectDir = tempDir.resolve("project").apply { mkdirs() }
            projectDir.resolve("README.md").writeText("new")
            val repoDir = createRepoDir()
            repoDir.resolve("README.md").writeText("old")

            ProfilePublisher.copyProfileFiles(
                profileFiles = listOf("README.md"),
                projectDir = projectDir,
                from = "",
                repoDir = repoDir,
                logger = logger
            )

            assertThat(repoDir.resolve("README.md")).hasContent("new")
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class WithCredentialsTest {

        @Test
        fun `withCredentials returns new config with updated credentials`() {
            val original = GitPushConfiguration(
                from = "src",
                to = "dst",
                repo = RepositoryConfiguration(
                    name = "test",
                    repository = "https://example.com.git",
                    credentials = RepositoryCredentials("oldU", "oldP")
                ),
                branch = "main",
                message = "msg"
            )

            val updated = original.withCredentials("newU", "newP")

            assertThat(updated.from).isEqualTo("src")
            assertThat(updated.to).isEqualTo("dst")
            assertThat(updated.repo.name).isEqualTo("test")
            assertThat(updated.repo.repository).isEqualTo("https://example.com.git")
            assertThat(updated.repo.credentials.username).isEqualTo("newU")
            assertThat(updated.repo.credentials.password).isEqualTo("newP")
            assertThat(updated.branch).isEqualTo("main")
            assertThat(updated.message).isEqualTo("msg")
        }
    }
}
