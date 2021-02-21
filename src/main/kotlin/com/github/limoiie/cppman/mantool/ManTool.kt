package com.github.limoiie.cppman.mantool

import com.github.limoiie.cppman.services.MyApplicationService

interface ManTool {
    fun manPage(word: String, section: String? = null): String?
    fun candidates(section: String? = null): Collection<String>

    fun fixSection(section: String?): String {
        if (section !in MyApplicationService.manSections) {
            return MyApplicationService.allSections
        }
        return section!!
    }
}