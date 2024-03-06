package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.mediaserver.jellyfin.JellyfinRestService
import com.github.schaka.janitorr.mediaserver.jellyfin.filesystem.PathStructure
import com.github.schaka.janitorr.mediaserver.jellyfin.library.LibraryType
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

abstract class MediaServerService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        @JvmStatic
        protected val seasonPattern = Regex("Season (?<season>\\d+)")
        private val filePattern = Regex("^.*\\.(mkv|mp4|avi|webm|mts|m2ts|ts|wmv|mpg|mpeg|mp2|m2v|m4v)\$")
        private val numberPattern = Regex("[0-9]+")
    }

    abstract fun cleanupTvShows(items: List<LibraryItem>)

    abstract fun cleanupMovies(items: List<LibraryItem>)

    abstract fun updateGoneSoon(type: LibraryType, items: List<LibraryItem>, onlyAddLinks: Boolean = false)

    protected fun isMediaFile(path: String) =
            filePattern.matches(path)

    fun parseMetadataId(value: String?): Int? {
        return value?.let {
            numberPattern.findAll(it)
                    .map(MatchResult::value)
                    .map(String::toInt)
                    .firstOrNull()
        }
    }

    protected fun createSymLink(source: Path, target: Path, type: String) {
        if (!Files.exists(target)) {
            log.debug("Creating {} link from {} to {}", type, source, target)
            Files.createSymbolicLink(target, source)
        } else {
            log.debug("{} link already exists from {} to {}", type, source, target)
        }
    }

    protected fun pathStructure(it: LibraryItem, leavingSoonParentPath: Path): PathStructure {
        val rootPath = Path.of(it.rootFolderPath)
        val itemFilePath = Path.of(it.filePath)
        val itemFolderName = itemFilePath.subtract(rootPath).firstOrNull()

        val fileOrFolder = itemFilePath.subtract(Path.of(it.parentPath)).firstOrNull() // contains filename and folder before it e.g. (Season 05) (ShowName-Episode01.mkv) or MovieName2013.mkv

        val sourceFolder = rootPath.resolve(itemFolderName)
        val sourceFile = sourceFolder.resolve(fileOrFolder)

        val targetFolder = leavingSoonParentPath.resolve(itemFolderName)
        val targetFile = targetFolder.resolve(fileOrFolder)

        return PathStructure(sourceFolder, sourceFile, targetFolder, targetFile)
    }
}
