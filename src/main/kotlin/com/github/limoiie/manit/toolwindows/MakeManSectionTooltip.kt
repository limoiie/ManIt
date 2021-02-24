package com.github.limoiie.manit.toolwindows

import com.github.limoiie.manit.database.dao.ManSection

const val allSections = "*"

const val defaultTooltip = "Unknown Section"

val manSectionTips = mapOf(
    allSections to "All Sections",
    "1" to "1. Executable programs or shell commands",
    "2" to "2. System calls (functions provided by the kernel)",
    "3" to "3. Library calls (functions within program libraries)",
    "4" to "4. Special files (usually found in /dev)",
    "5" to "5. File formats and conventions eg /etc/passwd",
    "6" to "6. Games",
    "7" to "7. Miscellaneous (including macro packages and conventions)",
    "8" to "8. System administration commands (usually only for root)",
    "9" to "9. Kernel routines",
    "l" to "l. Local documentation",
    "n" to "n. New manpage"
)

fun makeManSectionTooltip(manSections: List<ManSection>): List<String> {
    return manSections.map {
        manSectionTips[it.name]?: defaultTooltip
    }
}
