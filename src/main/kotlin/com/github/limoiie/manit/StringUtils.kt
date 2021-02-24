package com.github.limoiie.manit

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val PROCESS_TIMEOUT = 10L

fun String.runCommand(workingDir: File? = null): String? {
    return try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(parts)
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(PROCESS_TIMEOUT, TimeUnit.SECONDS)
        proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
