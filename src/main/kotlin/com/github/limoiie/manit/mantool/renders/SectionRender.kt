package com.github.limoiie.manit.mantool.renders

interface SectionRender {

    fun render(section: String?, lines: List<String>): String

    fun firstRender() = ""

    fun lastRender() = ""
}
