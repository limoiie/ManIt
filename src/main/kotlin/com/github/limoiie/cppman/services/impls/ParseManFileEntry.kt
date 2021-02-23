package com.github.limoiie.cppman.services.impls

import java.nio.file.Path
import java.nio.file.Paths

const val MIN_TITLE_PARTS = 3

fun parseManFileEntry(manSourcePath: Path, mainSection: String, manFilePath: Path): ManFileEntry? {
    var result: ManFileEntry? = null
    val manFile = manFilePath.toFile()
    if (manFile.isFile && manFile.canRead()) {
        manFile.forEachLine { line ->
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
                        makeKeywords(manFile.nameWithoutExtension, parts[1]),
                        makeSections(mainSection, parts[2]),
                        manSourcePath,
                        manFilePath
                    )
                }
                return@forEachLine
            }
            if (line.startsWith(".so")) { // link line
                val relativePath = Paths.get(line.substring(3).trim())
                result = ManFileEntry(
                    makeKeywords(manFile.nameWithoutExtension, null),
                    makeSections(mainSection, null),
                    manSourcePath,
                    manSourcePath.resolve(relativePath)
                )
                return@forEachLine
            }
        }
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
