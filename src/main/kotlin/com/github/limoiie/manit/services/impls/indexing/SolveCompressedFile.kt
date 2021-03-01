package com.github.limoiie.manit.services.impls.indexing

import com.github.limoiie.manit.services.ManConfigAppService
import com.intellij.openapi.components.service
import java.io.File

fun File.decompress(): File {
    return service<ManConfigAppService>()
        .decompress(path, extension) ?: this
}

fun File.getCompressSuffix(): String {
    return service<ManConfigAppService>()
        .getCompressSuffix(this)
}

fun File.nameWithoutCompressExtension(): String {
    val suffix = getCompressSuffix()
    return name.substring(0, name.length - suffix.length)
}
