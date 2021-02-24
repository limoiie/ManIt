package com.github.limoiie.manit.mantool.renders

import com.intellij.lang.documentation.DocumentationMarkup.*

class DefaultManpageRender {
    fun render(manpage: String): String {
        return "rendered $manpage"
    }

    private fun document(): String {
        return DEFINITION_START + "val string: String?" + DEFINITION_END + "\n" +
                CONTENT_START + "main description" + CONTENT_END + "\n" +
                SECTIONS_START + "\n" +
                createSection(1) +
                createSection(2) +
                createSection(3) +
                SECTIONS_END
    }

    private fun createSection(no: Int): String {
        return SECTION_HEADER_START + "Section Name - $no" + "\n" +
                SECTION_SEPARATOR + "<p> Content - $no: reset the scrollBar of " +
                "manPageLayout after update the text call invokeLater in other " +
                "thread rather than the ui one, so that the manPageLayout could " +
                "be updated before resetting its ScrollBar" +
                SECTION_END
    }
}
