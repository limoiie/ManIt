package com.github.limoiie.manit.services

import com.github.limoiie.manit.runCommand
import java.io.File

/**
 * A service parsing /etc/man.conf to provide some man utilities
 */
class ManConfigAppService {
    companion object {
        private const val SUFFIX_GZ = "gz"
//        private const val SUFFIX_TAR = "tar"

        private const val TMP_PREFIX = "manit"
        private const val TMP_SUFFIX = "tmp"
    }

    private val tools = mapOf(
        SUFFIX_GZ to listOf("gunzip", "-c")
    )

    fun getCompressSuffix(file: File): String {
        for (suffix in tools.keys) {
            if (file.name.endsWith(suffix)) {
                return ".$suffix"
            }
        }
        return ""
    }

    fun decompress(path: String, extension: String): File? {
        if (extension in tools) {
            val decompressed = (tools[extension]!! + path).runCommand()
            if (decompressed != null) {
                val file = File.createTempFile(TMP_PREFIX, TMP_SUFFIX)
                file.writeText(decompressed)
                file.deleteOnExit()
                return file
            }
        }
        return null
    }
}
