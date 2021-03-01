package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManFiles : IntIdTable() {
    private const val COLUMN_NAME_PATH = "path"
    private const val COLUMN_NAME_SOURCE = "source"
    private const val MAX_PATH_LEN = 255

    val path = varchar(COLUMN_NAME_PATH, MAX_PATH_LEN)
    val manSource = reference(COLUMN_NAME_SOURCE, ManSources)
}
