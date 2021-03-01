package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.sql.Table

object ManFileSections : Table() {
    private const val COLUMN_NAME_FILE = "file"
    private const val COLUMN_NAME_SECTION = "section"

    val file = reference(COLUMN_NAME_FILE, ManFiles)
    val section = reference(COLUMN_NAME_SECTION, ManSections)

    override val primaryKey = PrimaryKey(file, section)
}
