package com.github.limoiie.manit.mantool.renders

interface LineRender {

    fun render(rawLine: String): String

    fun firstRender() = ""

    fun lastRender() = ""
}
