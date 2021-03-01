package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManKeywords : IntIdTable() {

    private const val COLUMN_NAME_KEYWORD = "keyword"
    private const val COLUMN_NAME_FILE = "file"

    val keyword = text(COLUMN_NAME_KEYWORD)
    val file = reference(COLUMN_NAME_FILE, ManFiles)
}
