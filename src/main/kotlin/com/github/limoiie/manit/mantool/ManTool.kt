package com.github.limoiie.manit.mantool

import com.github.limoiie.manit.services.OuterManAppService

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
     * Set as [OuterManAppService.allSections] if [section] is invalid
     */
    fun fixSection(section: String?): String {
        if (section !in OuterManAppService.manSections) {
            return OuterManAppService.allSections
        }
        return section!!
    }
}
