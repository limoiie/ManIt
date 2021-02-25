package com.github.limoiie.manit.mantool.renders

class DefaultSectionRender(private val lineRender: LineRender) : SectionRender {
    override fun render(section: String?, lines: List<String>): String {
        val content = lineRender.firstRender() + renderLines(lines) +
                lineRender.lastRender()
        return """<div class="section">$section</div>""" + LINE_END +
                """<div style="margin-left:20px">$content</div>"""
    }

    private fun renderLines(lines: List<String>): String {
        return lines.joinToString(LINE_JOINT) { lineRender.render(it) }
    }
}
