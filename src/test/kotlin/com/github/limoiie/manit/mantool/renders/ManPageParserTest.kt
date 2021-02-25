package com.github.limoiie.manit.mantool.renders

import org.junit.jupiter.api.Assertions.assertEquals

internal class ManPageParserTest {

    @org.junit.jupiter.api.Test
    fun parseOneField() {
        val cases = mapOf(
            "Section" to "Section",
            "Section other fields" to "Section",
            "Section\\ One" to "Section\\ One",
            "  Section\\ One" to "Section\\ One",
            "\"Section\"" to "Section",
            "\"Section One\"" to "Section One",
            "  \"Section One\"" to "Section One",
            "\"Section One\" other fields" to "Section One",
        )

        for (case in cases) {
            println("Test `$case`")
            assertEquals(
                case.value,
                ManPageParser.parseHeaderField(case.key)
            )
        }
    }

    @org.junit.jupiter.api.Test
    fun parseHeaderFields() {
        val cases = mapOf(
            "section\\ one failed" to
                    listOf("section\\ one", "failed"),
            "  section\\ one  failed" to
                    listOf("section\\ one", "failed"),
            "\"Section One Failed\" is good" to
                    listOf("Section One Failed", "is", "good"),
            "\"Section One Failed\"  is\\ good" to
                    listOf("Section One Failed", "is\\ good"),
        )

        for (case in cases) {
            println("Test `$case`")
            assertEquals(
                case.value,
                ManPageParser.parseHeaderFields(case.key)
            )
        }
    }

    @org.junit.jupiter.api.Test
    fun parseInlineFontDecorator() {
        val cases = mapOf(
            "start\\fI-display\\fP-good" to listOf(
                "" to "start",
                "\\fI" to "-display",
                "\\fP" to "-good"
            )
        )

        for (case in cases) {
            val output = ManPageParser.parseInlineDecorator(case.key).toList()
            output.forEach {
                println("$it")
            }
            assertEquals(case.value, output)
        }
    }
}
