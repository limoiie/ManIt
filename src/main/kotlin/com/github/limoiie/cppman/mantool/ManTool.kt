package com.github.limoiie.cppman.mantool

interface ManTool {
    fun manPage(word: String): String?
    fun candidates(): Collection<String>
}