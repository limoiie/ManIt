package com.github.limoiie.manit.mantool.index

import java.nio.file.Path

data class ManFileIndexEntry(
    val keywords: List<String>,
    val sections: List<String>,
    val manSource: Path, // absolute path
    val manFile: Path // absolute path
)
