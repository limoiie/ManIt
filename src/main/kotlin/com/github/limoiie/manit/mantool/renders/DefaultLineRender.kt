package com.github.limoiie.manit.mantool.renders

import com.intellij.util.containers.Stack
import com.github.limoiie.manit.mantool.parser.ManPageTag as T
import com.github.limoiie.manit.mantool.parser.ManPageParser as P

class DefaultLineRender : LineRender {
    // parser states
    private val fontStack = Stack<String>()

    override fun render(rawLine: String): String {
        val (macro, line) = splitMacroLine(rawLine)
        val renderedLine = line
            .run { renderEscapeCharacters(this) }
            .run { renderInlineTag(this) }
        if (macro != null) {
            return renderMacroLine(macro, renderedLine)
        }
        return "$renderedLine<br />"
    }

    override fun firstRender(): String = String()

    override fun lastRender(): String {
        return tagInlineFont(false)
    }

    private fun splitMacroLine(rawLine: String): Pair<String?, String> {
        // fixme - macro does not always end with blank, for example: .fi\fR
        val parts = rawLine.split(' ', limit = 2)
        val macro = parts.getOrNull(0)
        val line = parts.getOrNull(1)
        return if (macro != null && macro.isMacro()) {
            macro to (line ?: "")
        } else null to rawLine
    }

    /**
     * todo
     *  - Support more macros
     *  - refactor into a more flexible design
     */
    private fun renderMacroLine(macro: String, line: String): String {
        return when (macro) {
            T.FONT_BOLD -> "<b>$line</b>"
            T.FONT_BOLD_ITALIC -> "<b><i>$line</i></b>"
            T.FONT_BOLD_ROMAN -> "<b><u>$line</u></b>"
            T.FONT_ITALIC -> "<i>$line</i>"
            T.FONT_ITALIC_BOLD -> "<i><b>$line</b></i>"
            T.FONT_ITALIC_ROMAN -> "<i><u>$line</u></i>"
            T.FONT_ROMAN_BOLD -> "<u><b>$line</b></u>"
            T.FONT_ROMAN_ITALIC -> "<u><i>$line</i></u>"
            T.FONT_SMALL_BOLD -> "<small><b>$line</b></small>"
            T.FONT_SMALL -> "<small>$line</small>"
            T.PARA_BEGIN_LP -> "<p>$line"
            T.PARA_BEGIN_P -> "<p>$line"
            T.PARA_BEGIN_PP -> "<p>$line"
            T.PARA_SPLIT -> "<br /><br />$line"
            else -> "$macro $line"
        }
    }

    private fun renderEscapeCharacters(line: String): String {
        return line
            .replace("  ", "&nbsp; ")
            .replace("<", "&lt;")
    }

    private fun renderInlineTag(line: String?): String {
        if (line == null || line.isBlank()) return ""

        return P.parseInlineDecorator(line)
            .map { (tag, content) ->
                when (tag) {
                    T.INLINE_FONT_BOLD, T.INLINE_FONT_ITALIC,
                    T.INLINE_FONT_ROMAN, T.INLINE_FONT_PREV ->
                        renderInlineFontTag(tag, content)
                    else -> content
                }
            }
            .joinToString("")
    }

    private fun renderInlineFontTag(fontTag: String, content: String): String {
        val closedPrevTag = tagInlineFont(false)
        if (fontTag == T.INLINE_FONT_PREV) {
            fontStack.tryPop()
        } else {
            fontStack.push(fontTag)
        }
        val openedCurrTag = tagInlineFont(true)
        return closedPrevTag + openedCurrTag + content
    }

    private fun tagInlineFont(open: Boolean): String {
        if (!fontStack.isEmpty()) {
            // close the previous
            return when (fontStack.peek()) {
                T.INLINE_FONT_BOLD -> if (open) "<b>" else "</b>"
                T.INLINE_FONT_ITALIC -> if (open) "<i>" else "</i>"
                T.INLINE_FONT_ROMAN -> if (open) "<tt>" else "</tt>"
                else -> ""
            }
        }
        return ""
    }

    private fun String.isMacro(): Boolean {
        return startsWith('.')
    }
}
