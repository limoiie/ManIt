package com.github.limoiie.cppman.mantool

import com.github.limoiie.cppman.runCommand
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger

class Man(private val man : String = "man") : ManTool {
    private val logger = logger<Man>()

    private val candidates by lazy {
        val list = "$man -f .".runCommand()
        val candidates = list?.splitToSequence('\n')
            ?.map { it.split('-').first().trim() }
            ?.toList() ?: listOf()
        logger.debug { "Man candidates size: ${candidates.size}" }
        candidates
    }

    override fun manPage(word: String): String? {
        val page = "$man -P cat $word".runCommand()
        logger.debug { page?: "Failed to run $man" }
        return page
    }

    override fun candidates(): Collection<String> = candidates

}