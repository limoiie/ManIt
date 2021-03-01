package com.github.limoiie.manit.services.impls.indexing

import com.github.limoiie.manit.mantool.renders.ManPageParser
import com.github.limoiie.manit.mantool.renders.isPageHeaderLine
import java.io.File
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
    val manFile = manFilePath.toFile()
    if (manFile.isFile && manFile.canRead()) {
        val nameWithoutExtension = nameIgnoreSectionSuffix(manFile)
        manFile.decompress().forEachLine { line ->
            // method 1: get man keywords from the section
            if (line.isPageHeaderLine()) {
                val parts = ManPageParser.parseHeaderFields(line)
                    .filterNot(String::isBlank)
                    .toList()
                if (parts.size >= MIN_TITLE_PARTS) {
                    result = ManFileEntry(
                        makeKeywords(nameWithoutExtension, parts[1]),
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
                    makeKeywords(nameWithoutExtension, null),
                    makeSections(mainSection, null),
                    manSourcePath,
                    manSourcePath.resolve(relativePath)
                )
                return@forEachLine
            }
        }
        // fallback method:
        result = ManFileEntry(
            makeKeywords(nameWithoutExtension, null),
            makeSections(mainSection, null),
            manSourcePath,
            manFilePath
        )
    }
    return result
}

private fun makeKeywords(manFileName: String, nameInTitle: String?): List<String> {
    // todo for stdman (maybe split the filename is not a good choice)
    //  - std::something::operator-=,+=
    //  - std::something::path1,path2 or std::equal_to,less,...
    //  - std::hash (std::string, std::wstring, std::u16string, ...)
    //  - std::chrono::duration<Rep,Period>
    val keywordsByFileName = manFileName.split(',')
    val keywordByTitle = nameInTitle?.toLowerCase()
    if (keywordByTitle !in keywordsByFileName && keywordByTitle != null) {
        return listOf(keywordByTitle) + keywordsByFileName
    }
    return keywordsByFileName
}

private fun makeSections(mainSection: String, sectionInTitle: String?): List<String> {
    if (mainSection != sectionInTitle && sectionInTitle != null) {
        return listOf(mainSection, sectionInTitle)
    }
    return listOf(mainSection)
}

private fun nameIgnoreSectionSuffix(file: File): String {
    val name = file.nameWithoutCompressExtension()
    return File(name).nameWithoutExtension
}
