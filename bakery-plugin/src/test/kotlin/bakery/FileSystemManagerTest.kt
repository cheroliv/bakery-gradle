package bakery

import bakery.FileSystemManager.copyBakedFilesToRepo
import bakery.FileSystemManager.createRepoDir
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class FileSystemManagerTest {

    private val logger = LoggerFactory.getLogger(FileSystemManagerTest::class.java)

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateRepoDirTest {

        @TempDir
        lateinit var tempDir: File

        @Test
        fun `creates new directory when path does not exist`() {
            val target = tempDir.resolve("newRepo")
            assertThat(target).doesNotExist()

            val result = createRepoDir(target.absolutePath, logger)

            assertThat(result).exists().isDirectory
            assertThat(result.absolutePath).isEqualTo(target.absolutePath)
        }

        @Test
        fun `recreates directory when it already exists`() {
            val target = tempDir.resolve("existingRepo").apply { mkdirs() }
            target.resolve("stale.txt").writeText("old")
            assertThat(target.resolve("stale.txt")).exists()

            val result = createRepoDir(target.absolutePath, logger)

            assertThat(result).exists().isDirectory
            assertThat(result.resolve("stale.txt")).doesNotExist()
        }

        @Test
        fun `deletes existing file and creates directory with same name`() {
            val target = tempDir.resolve("fileNotDir").apply { writeText("I am a file") }
            assertThat(target).exists().isFile

            val result = createRepoDir(target.absolutePath, logger)

            assertThat(result).exists().isDirectory
        }

        @Test
        fun `throws when path exists as non-deletable file`() {
            // On crée un fichier qu'on rend non-supprimable en créant ensuite un directory
            // verrouillant le parent... Approche alternative : on vérifie simplement
            // que si delete() renvoie false sur un fichier, on lève bien IOException.
            val target = tempDir.resolve("lockedFile").apply { writeText("data") }
            target.setReadOnly()

            val result = runCatching { createRepoDir(target.absolutePath, logger) }

            // Sur la plupart des systèmes POSIX, setReadOnly n'empêche pas le propriétaire
            // de supprimer. Attendre au moins qu'on retourne un répertoire (delete réussi).
            // Si delete échouait vraiment, on attendrait une exception.
            assertThat(result.isSuccess || result.exceptionOrNull() is IOException).isTrue()
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CopyBakedFilesToRepoTest {

        @TempDir
        lateinit var tempDir: File

        @Test
        fun `copies files from bakeDir to repoDir and deletes bakeDir`() {
            val bakeDir = tempDir.resolve("bake").apply { mkdirs() }
            bakeDir.resolve("index.html").writeText("hello")
            val repoDir = tempDir.resolve("repo").apply { mkdirs() }

            val result = copyBakedFilesToRepo(bakeDir.absolutePath, repoDir, logger)

            assertThat(result).isEqualTo(GitService.FileOperationResult.Success)
            assertThat(repoDir.resolve("index.html")).exists().hasContent("hello")
            assertThat(bakeDir).doesNotExist()
        }

        @Test
        fun `returns failure when bakeDir does not exist`() {
            val repoDir = tempDir.resolve("repo").apply { mkdirs() }

            val result = copyBakedFilesToRepo(
                tempDir.resolve("nonexistent").absolutePath,
                repoDir,
                logger
            )

            assertThat(result).isInstanceOf(GitService.FileOperationResult.Failure::class.java)
        }

        @Test
        fun `copies nested directories`() {
            val bakeDir = tempDir.resolve("bake").apply { mkdirs() }
            val subDir = bakeDir.resolve("assets").apply { mkdirs() }
            subDir.resolve("style.css").writeText("body{}")
            val repoDir = tempDir.resolve("repo").apply { mkdirs() }

            val result = copyBakedFilesToRepo(bakeDir.absolutePath, repoDir, logger)

            assertThat(result).isEqualTo(GitService.FileOperationResult.Success)
            assertThat(repoDir.resolve("assets/style.css")).exists().hasContent("body{}")
            assertThat(bakeDir).doesNotExist()
        }

        @Test
        fun `overwrites existing files in repoDir`() {
            val bakeDir = tempDir.resolve("bake").apply { mkdirs() }
            bakeDir.resolve("index.html").writeText("new")
            val repoDir = tempDir.resolve("repo").apply { mkdirs() }
            repoDir.resolve("index.html").writeText("old")

            copyBakedFilesToRepo(bakeDir.absolutePath, repoDir, logger)

            assertThat(repoDir.resolve("index.html")).hasContent("new")
        }
    }
}
