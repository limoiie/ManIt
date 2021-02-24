package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

private const val COLUMN_NAME_PATH = "path"
private const val MAX_PATH_LEN = 255

object ManSources : IntIdTable() {
    val path = varchar(COLUMN_NAME_PATH, MAX_PATH_LEN).uniqueIndex()
}
