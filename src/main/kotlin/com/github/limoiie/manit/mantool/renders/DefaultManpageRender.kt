package com.github.limoiie.manit.mantool.renders

const val LINE_END = "\n"
const val LINE_JOINT = " "

class DefaultManpageRender(private val sectionRender: SectionRender) {

    fun render(manpage: String): String {
        val sections = manpage.lineSequence().splitSections()
        return sectionRender.firstRender() + renderSections(sections) +
                sectionRender.lastRender()
    }

    private fun Sequence<String>.splitSections(): Map<String?, List<String>> {
        var sectionName: String? = null
        return filterNot { it.isCommentLine() }
            .map { line ->
                if (line.isSectionHeaderLine()) {
                    sectionName = ManPageParser.parseHeaderFields(line).getOrNull(1)
                }
                sectionName to line
            }
            .groupBy({ it.first }) { it.second }
    }

    private fun renderSections(sections: Map<String?, List<String>>): String {
        return sections.map { sectionRender.render(it.key, it.value) }
            .joinToString(LINE_END)
    }

    // process states
}
