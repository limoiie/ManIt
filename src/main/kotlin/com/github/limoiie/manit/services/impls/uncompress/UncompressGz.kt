package com.github.limoiie.manit.services.impls.uncompress

import java.io.File

fun uncompressGz(file: File): String {
    return "<i>Failed to load manpage: .gz is not supported yet!</i>"
}

fun uncompressGz(path: String): String {
    return uncompressGz(File(path))
}
