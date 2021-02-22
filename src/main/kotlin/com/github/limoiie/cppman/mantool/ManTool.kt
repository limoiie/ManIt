package com.github.limoiie.cppman.mantool

import com.github.limoiie.cppman.services.MyApplicationService

interface ManTool {

    /**
     * Get the manpage of [word] in [section]
     */
    fun manPage(word: String, section: String? = null): String?

    /**
     * Get candidate completion list by [section]
     */
    fun candidates(section: String? = null): Collection<String>

    /**
     * Set as [MyApplicationService.allSections] if [section] is invalid
     */
    fun fixSection(section: String?): String {
        if (section !in MyApplicationService.manSections) {
            return MyApplicationService.allSections
        }
        return section!!
    }
}
