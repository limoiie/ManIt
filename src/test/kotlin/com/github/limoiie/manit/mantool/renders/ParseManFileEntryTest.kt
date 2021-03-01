package com.github.limoiie.manit.mantool.renders

import org.junit.jupiter.api.Test
import java.io.File

class ParseManFileEntryTest {
    @Test
    fun testFileNameWithoutExtension() {
        val name = File("NoSuchFile.txt").nameWithoutExtension
        println(name)
    }
}
