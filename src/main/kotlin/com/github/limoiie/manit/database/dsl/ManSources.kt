package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManSources : IntIdTable() {

    private const val COLUMN_NAME_PATH = "path"
    private const val COLUMN_NAME_INDEXED = "indexed"
    private const val MAX_PATH_LEN = 255

    val path = varchar(COLUMN_NAME_PATH, MAX_PATH_LEN).uniqueIndex()
    val indexed = bool(COLUMN_NAME_INDEXED).default(false)
}
