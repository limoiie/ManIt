package com.github.limoiie.manit.services.impls.indexing

import java.nio.file.Path
import java.nio.file.Paths

private const val MIN_TITLE_PARTS = 3
private const val MANPAGE_LINK_PREFIX = ".so"

fun parseManFileEntry(
    manSourcePath: Path,
    mainSection: String,
    manFilePath: Path
): ManFileEntry? {
    var result: ManFileEntry? = null
    val decompressedManFile = manFilePath.toFile().decompress()
    if (decompressedManFile.isFile && decompressedManFile.canRead()) {
        decompressedManFile.forEachLine { line ->
            // method 1: get man keywords from the section
            if (line.startsWith(".TH") || // header line
                line.startsWith(".Dt") ||
                line.startsWith(".HS")
            ) {
                // fixme the word or the section may contains blank like:
                //  - "string at"
                //  - string\ at
                val parts = line.split(Regex("\\s"))
                    .filterNot(String::isBlank)
                    .toList()
                if (parts.size >= MIN_TITLE_PARTS) {
                    result = ManFileEntry(
                        makeKeywords(decompressedManFile.nameWithoutExtension, parts[1]),
                        makeSections(mainSection, parts[2]),
                        manSourcePath,
                        manFilePath
                    )
                }
                return@forEachLine
            }
            // method 2: if this man file link to the other file, get man keywords from
            // the filename and set the file as the linked path
            if (line.startsWith(MANPAGE_LINK_PREFIX)) { // link line
                val relativePath =
                    Paths.get(line.substring(MANPAGE_LINK_PREFIX.length).trim())
                result = ManFileEntry(
                    makeKeywords(decompressedManFile.nameWithoutExtension, null),
                    makeSections(mainSection, null),
                    manSourcePath,
                    manSourcePath.resolve(relativePath)
                )
                return@forEachLine
            }
        }
        // fallback method:
        result = ManFileEntry(
            makeKeywords(decompressedManFile.nameWithoutExtension, null),
            makeSections(mainSection, null),
            manSourcePath,
            manFilePath
        )
    }
    return result
}

private fun makeKeywords(manFileName: String, nameInTitle: String?): List<String> {
    val keywordByFileName = manFileName.toLowerCase()
    val keywordByTitle = nameInTitle?.toLowerCase()
    if (keywordByFileName != keywordByTitle && keywordByTitle != null) {
        return listOf(keywordByFileName, keywordByTitle)
    }
    return listOf(keywordByFileName)
}

private fun makeSections(mainSection: String, sectionInTitle: String?): List<String> {
    if (mainSection != sectionInTitle && sectionInTitle != null) {
        return listOf(mainSection, sectionInTitle)
    }
    return listOf(mainSection)
}
