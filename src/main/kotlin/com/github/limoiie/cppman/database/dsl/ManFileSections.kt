package com.github.limoiie.cppman.database.dsl

import org.jetbrains.exposed.sql.Table

object ManFileSections : Table() {
    val file = reference("file", ManFiles)
    val section = reference("section", ManSections)

    override val primaryKey = PrimaryKey(file, section)
}
