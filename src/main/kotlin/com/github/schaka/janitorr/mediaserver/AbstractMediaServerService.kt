package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.cleanup.CleanupType
import com.github.schaka.janitorr.mediaserver.filesystem.PathStructure
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.stats.StatsClientProperties
import org.slf4j.LoggerFactory
import org.springframework.util.FileSystemUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

abstract class AbstractMediaServerService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)

        @JvmStatic
        protected val seasonPatternLanguageAgnostic = Regex("(\\w+) (?<season>\\d+)")
        private val filePattern = Regex("^.*\\.(mkv|mp4|avi|webm|mts|m2ts|ts|wmv|mpg|mpeg|mp2|m2v|m4v)$")
        private val numberPattern = Regex("[0-9]+")
    }

    abstract fun cleanupTvShows(items: List<LibraryItem>)

    abstract fun cleanupMovies(items: List<LibraryItem>)

    abstract fun populateMediaServerIds(items: List<LibraryItem>, type: LibraryType, config: StatsClientProperties)

    abstract fun updateLeavingSoon(cleanupType: CleanupType, libraryType: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean = false)

    protected fun isMediaFile(path: String) =
        filePattern.matches(path)

    internal fun parseMetadataId(value: String?): Int? {
        return value?.let {
            numberPattern.findAll(it)
                .map(MatchResult::value)
                .map(String::toInt)
                .firstOrNull()
        }
    }

    private fun createSymLink(source: Path, target: Path, type: String) {
        if (!Files.exists(target)) {
            log.debug("Creating {} link from {} to {}", type, source, target)
            Files.createSymbolicLink(target, source)
        } else {
            log.debug("{} link already exists from {} to {}", type, source, target)
        }
    }

    private fun copyExtraFiles(files: List<String>, target: Path) {
         // TODO: files already contain the full path, consider only adding the filename to an existing source (folder)
        for (filePath in files) {
            val source = Path(filePath)
            val targetFolder = target.parent
            val targetFile = targetFolder.resolve(source.fileName)
            Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING)
            log.debug("Copying extra files from {} to {}", filePath, targetFile)
        }
    }

    internal fun pathStructure(it: LibraryItem, leavingSoonParentPath: Path): PathStructure {
        val rootPath = Path(it.rootFolderPath)
        val itemFilePath = Path(it.filePath)
        val fileName = itemFilePath.last()

        // this can contain any amount of subfolders that a movie or TV show may be placed under
        val itemFolderName = itemFilePath
            .subtract(rootPath)
            .reduce(this::combinePaths)
            .subtract(fileName)
            .reduce(this::combinePaths)

        // contains filename and folder before it e.g. (Season 05) (ShowName-Episode01.mkv) or MovieName2013.mkv
        val fileOrFolder = itemFilePath.subtract(Path(it.parentPath)).firstOrNull()
        val duplicateFolder = itemFolderName.last() == fileOrFolder

        val sourceFolder = if (duplicateFolder) removePath(rootPath.resolve(itemFolderName), fileOrFolder) else rootPath.resolve(itemFolderName)
        val sourceFile = sourceFolder.resolve(fileOrFolder)

        val targetFolder = if (duplicateFolder) removePath(leavingSoonParentPath.resolve(itemFolderName), fileOrFolder) else leavingSoonParentPath.resolve(itemFolderName)
        val targetFile = targetFolder.resolve(fileOrFolder)

        return PathStructure(sourceFolder, sourceFile, targetFolder, targetFile)
    }

    fun combinePaths(a: Path, b: Path): Path {
        return a.resolve(b)
    }

    fun removePath(source: Path, toRemove: Path): Path {
        val newPath = source.subtract(toRemove).reduce(this::combinePaths)

        if (source.isAbsolute && !newPath.isAbsolute) {
            return newPath.toAbsolutePath()
        }
        if (newPath.root != source.root) {
            return source.root.resolve(newPath)
        }
        return newPath
    }

    fun cleanupPath(leavingSoonDir: String, libraryType: LibraryType, cleanupType: CleanupType) {
        val path = Path(leavingSoonDir, libraryType.folderName, cleanupType.folderName)
        FileSystemUtils.deleteRecursively(path)
        Files.createDirectories(path)
    }

    protected fun createLinks(items: List<LibraryItem>, path: Path, type: LibraryType) {
        items.forEach {
            try {

                // FIXME: Figure out if we're dealing with single episodes in a season when season folders are deactivated in Sonarr
                // Idea: If we did have an item for every episode in a season, this might work
                // For now, just assume season folders are always activated
                val structure = pathStructure(it, path)

                if (type == LibraryType.TV_SHOWS && it.season != null && !isMediaFile(structure.sourceFile.toString())) {
                    // TV Shows
                    val sourceSeasonFolder = structure.sourceFile
                    val targetSeasonFolder = structure.targetFile
                    log.trace("Season folder - Source: {}, Target: {}", sourceSeasonFolder, targetSeasonFolder)

                    if (sourceSeasonFolder.exists()) {
                        log.trace("Creating season folder {}", targetSeasonFolder)
                        Files.createDirectories(targetSeasonFolder)

                        val files = sourceSeasonFolder.listDirectoryEntries().filter { f -> isMediaFile(f.toString()) }
                        for (file in files) {
                            val fileName = file.subtract(sourceSeasonFolder).firstOrNull()!!

                            val source = sourceSeasonFolder.resolve(fileName)
                            val target = targetSeasonFolder.resolve(fileName)
                            createSymLink(source, target, "episode")
                            copyExtraFiles(it.extraFiles, target)
                        }

                    } else {
                        log.info("Can't find original season folder - no links to create {}", sourceSeasonFolder)
                    }
                } else if (type == LibraryType.MOVIES) {
                    // Movies
                    val source = structure.sourceFile
                    log.trace("Movie folder - {}", structure)

                    if (source.exists()) {
                        val target = structure.targetFile
                        Files.createDirectories(structure.targetFolder)
                        createSymLink(source, target, "movie")
                        copyExtraFiles(it.extraFiles, target)
                    } else {
                        log.info("Can't find original movie folder - no links to create {}", source)
                    }
                }
            } catch (e: Exception) {
                if (log.isDebugEnabled){
                    log.error("Couldn't find path {} - {}", it.parentPath, it, e)
                } else {
                    log.error("Couldn't find path {}", it.parentPath)
                }
            }
        }
    }
}
