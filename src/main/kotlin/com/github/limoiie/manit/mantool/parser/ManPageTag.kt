package com.github.limoiie.manit.mantool.parser

object ManPageTag {
    const val PAGE_START1 = """.TH"""
    const val PAGE_START2 = """.Dt"""
    const val PAGE_START3 = """.HS"""

    const val SECTION_START = """.SH"""
    const val SUB_SECTION_START = """.SS"""
    const val COMMENT_START = """.\""""

    // font macros

    const val FONT_BOLD = """.B"""
    const val FONT_BOLD_ITALIC = """.BI"""
    const val FONT_BOLD_ROMAN = """.BR"""
    const val FONT_ITALIC = """.I"""
    const val FONT_ITALIC_BOLD = """.IB"""
    const val FONT_ITALIC_ROMAN = """.IR"""
    const val FONT_ROMAN_BOLD = """.RB"""
    const val FONT_ROMAN_ITALIC = """.RI"""
    const val FONT_SMALL_BOLD = """.SB"""
    const val FONT_SMALL = """.SM"""

    // paragraph macros

    const val PARA_BEGIN_LP = """.LP"""
    const val PARA_BEGIN_P = """.P"""
    const val PARA_BEGIN_PP = """.PP"""
    const val PARA_SPLIT = """.sp"""

    const val INLINE_FONT_BOLD = """\fB"""
    const val INLINE_FONT_ITALIC = """\fI"""
    const val INLINE_FONT_ROMAN = """\fR"""
    const val INLINE_FONT_PREV = """\fP"""
}
