package com.github.limoiie.manit.mantool.loader

import java.io.File

object ManPageRawLoader {
    fun loadManPage(manFilePath: String): String {
        val manFile = File(manFilePath)
        if (!manFile.isFile || !manFile.canRead()) {
            return "<i>Failed to load manpage: $manFilePath is not readable!</i>"
        }
        return manFile.decompress().readText()
    }
}
