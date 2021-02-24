package com.github.limoiie.manit.services.impls

import java.nio.file.Path

data class ManFileEntry(
    val keywords: List<String>,
    val sections: List<String>,
    val manSource: Path, // absolute path
    val manFile: Path // absolute path
)
