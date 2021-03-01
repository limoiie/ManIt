package com.github.limoiie.manit.mantool.renders

object ManPageParser {
    /**
     * A header field is
     *   - either a string quoted by `"'
     *   - or a list of words joined by `\ '
     *   - or simply just a word contains no blank
     */
    private val sHeaderField = """\s*(?:"(.*)"|((?:\\\s|\S)*))""".toRegex()

    private val sInlineDecorator = """(\\f[PBIR])?([^\\]*)""".toRegex()

    fun parseHeaderField(s: String): String? {
        val r = sHeaderField.find(s)
        return r?.groupValues?.getOrNull(1)?.nullIfEmpty()
            ?: r?.groupValues?.getOrNull(2)
    }

    fun parseHeaderFields(s: String): List<String> {
        return sHeaderField.findAll(s)
            .map {
                it.groupValues.getOrNull(1)?.nullIfEmpty()
                    ?: it.groupValues.getOrNull(2)
            }
            .filterNotNull()
            .filter { it.isNotEmpty() }
            .toList()
    }

    fun parseInlineDecorator(s: String): Sequence<Pair<String, String>> {
        return sInlineDecorator.findAll(s)
            .filterNot { it.groupValues[1].isEmpty() && it.groupValues[2].isEmpty() }
            .map {
                it.groupValues[1] to it.groupValues[2]
            }
    }

    private fun String.nullIfEmpty(): String? {
        return if (this.isEmpty()) null else this
    }
}

fun String.isPageHeaderLine(): Boolean {
    return startsWith(ManPageTag.PAGE_START1, true) ||
            startsWith(ManPageTag.PAGE_START2, true) ||
            startsWith(ManPageTag.PAGE_START3, true)
}

fun String.isSectionHeaderLine(): Boolean {
    return startsWith(ManPageTag.SECTION_START, true) ||
            startsWith(ManPageTag.SUB_SECTION_START, true)
}

fun String.isCommentLine(): Boolean {
    return startsWith(ManPageTag.COMMENT_START)
}
