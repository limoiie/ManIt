package com.github.limoiie.manit.mantool.renders

class DefaultSectionRender(private val lineRender: LineRender) : SectionRender {
    override fun render(section: String?, lines: List<String>): String {
        val content = lineRender.firstRender() + renderLines(lines.drop(1)) +
                lineRender.lastRender()
        return wrapSection(section, content)
    }

    private fun renderLines(lines: List<String>): String {
        return lines.joinToString(LINE_END) { lineRender.render(it) }
    }

    private fun wrapSection(section: String?, content: String): String {
        val sectionIndent = "margin-left:20px"
        return """
            <div class="section">$section</div>
            <div style="$sectionIndent">$content</div>
        """.trimIndent()
    }
}
