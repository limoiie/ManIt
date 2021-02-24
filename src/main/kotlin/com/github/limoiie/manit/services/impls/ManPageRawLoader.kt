package com.github.limoiie.manit.services.impls

import com.github.limoiie.manit.services.impls.uncompress.uncompressGz
import java.io.File

private const val GZ = ".gz"

object ManPageRawLoader {
    fun loadManPage(manFilePath: String): String {
        val manFile = File(manFilePath)
        if (!manFile.isFile || !manFile.canRead()) {
            return "<i>Failed to load manpage: $manFilePath is not readable!</i>"
        }
        return when (manFile.extension) {
            GZ -> uncompressGz(manFile)
            else -> manFile.readText()
        }
    }
}
