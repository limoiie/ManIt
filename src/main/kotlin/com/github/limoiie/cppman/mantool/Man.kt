package com.github.limoiie.cppman.mantool

import com.github.limoiie.cppman.runCommand
import com.github.limoiie.cppman.services.MyApplicationService
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger

class Man(private val man : String = "man") : ManTool {
    private val logger = logger<Man>()

    private val candidates by lazy {
        val list = "$man -f .".runCommand()
        val candidates = list?.splitToSequence('\n')  // lines: <line> \n <line> ... => <line>*
            ?.map { it.split(" - ").first() }   // line: <words> - <description> => <words>*
            ?.flatMap { it.split(',').asSequence() }  // words: <word>, <word> ... => <word>*
            ?.map { it.trim() }
            ?.map { fnParseWord(it) }  // word: <token>(<section>) => (<section> -> <token>)*
            ?.groupBy({ it.first.subSequence(0, 1) }) {it.second}  //
            ?: mapOf()

        logger.debug { "Man candidates size: ${candidates.size}" }
        candidates
    }

    override fun manPage(word: String, section: String?): String? {
        var sec = fixSection(section)
        if (sec == MyApplicationService.allSections) {
            sec = ""
        }

        val page = "$man $sec $word".runCommand()
        logger.debug { page?: "Failed to run $man" }
        return page
    }

    override fun candidates(section: String?): Collection<String> {
        val sec = fixSection(section)
        return candidates[sec]?:
        candidates.asSequence()  // (section -> words)*
            .flatMap { it.value.asSequence() }  // // (word)*
            .toList()
    }

    /**
     * Parse [word], which is like keyword(section), into pair of section 'to' keyword
     */
    private fun fnParseWord(word: String): Pair<String, String> {
        var section = "n"
        var keyword = word

        // <keyword>(<section>)
        val re = Regex("(.*)\\((.*)\\).*")
        val r = re.matchEntire(word)
        if (r != null && r.groupValues.size == 3) {
            section = r.groupValues[2]
            keyword = r.groupValues[1]
        }

        return section to keyword
    }

}