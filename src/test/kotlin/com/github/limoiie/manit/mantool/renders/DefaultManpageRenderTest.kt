package com.github.limoiie.manit.mantool.renders

import org.junit.jupiter.api.Test
import java.io.File

internal class DefaultManpageRenderTest {

    @Test
    fun render() {
        val file = File("/usr/local/share/man/man3/FcCacheCopySet.3")
        val render = DefaultManpageRender(DefaultSectionRender(DefaultLineRender()))
        println("raw: ${file.readText()}")
        val rendered = render.render(file.readText())
        println("rendered: $rendered")
    }
}
