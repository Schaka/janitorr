package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.cleanup.CleanupType
import com.github.schaka.janitorr.jellystat.JellystatProperties
import com.github.schaka.janitorr.mediaserver.filesystem.PathStructure
import com.github.schaka.janitorr.mediaserver.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.util.FileSystemUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

abstract class AbstractMediaServerService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)

        @JvmStatic
        protected val seasonPattern = Regex("Season (?<season>\\d+)")

        @JvmStatic
        protected val seasonPatternLanguageAgnostic = Regex("(\\w+) (?<season>\\d+)")
        private val filePattern = Regex("^.*\\.(mkv|mp4|avi|webm|mts|m2ts|ts|wmv|mpg|mpeg|mp2|m2v|m4v)\$")
        private val numberPattern = Regex("[0-9]+")
    }

    abstract fun cleanupTvShows(items: List<LibraryItem>)

    abstract fun cleanupMovies(items: List<LibraryItem>)

    abstract fun populateMediaServerIds(items: List<LibraryItem>, type: LibraryType, config: JellystatProperties)

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
            val source = Path.of(filePath)
            val targetFolder = target.parent
            Files.copy(source, targetFolder, StandardCopyOption.REPLACE_EXISTING)
            log.debug("Copying extra files from {} to {}", filePath, target)
        }
    }

    internal fun pathStructure(it: LibraryItem, leavingSoonParentPath: Path): PathStructure {
        val rootPath = Path.of(it.rootFolderPath)
        val itemFilePath = Path.of(it.filePath)
        val itemFolderName = itemFilePath.subtract(rootPath).firstOrNull()

        // contains filename and folder before it e.g. (Season 05) (ShowName-Episode01.mkv) or MovieName2013.mkv
        val fileOrFolder = itemFilePath.subtract(Path.of(it.parentPath)).firstOrNull()

        val sourceFolder = rootPath.resolve(itemFolderName)
        val sourceFile = sourceFolder.resolve(fileOrFolder)

        val targetFolder = leavingSoonParentPath.resolve(itemFolderName)
        val targetFile = targetFolder.resolve(fileOrFolder)

        return PathStructure(sourceFolder, sourceFile, targetFolder, targetFile)
    }

    fun cleanupPath(leavingSoonDir: String, libraryType: LibraryType, cleanupType: CleanupType) {
        val path = Path.of(leavingSoonDir, libraryType.folderName, cleanupType.folderName)
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
                    log.error("Couldn't find path {}", it.parentPath, e)
                } else {
                    log.error("Couldn't find path {}", it.parentPath)
                }
            }
        }
    }
}
