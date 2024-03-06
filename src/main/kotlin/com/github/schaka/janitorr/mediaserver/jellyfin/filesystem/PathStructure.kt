package com.github.schaka.janitorr.mediaserver.jellyfin.filesystem

import java.nio.file.Path

data class PathStructure(
    val sourceFolder: Path,
    val sourceFile: Path,
    val targetFolder: Path,
    val targetFile: Path
) {
}