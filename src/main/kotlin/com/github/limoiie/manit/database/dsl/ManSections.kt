package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManSections : IntIdTable() {

    private const val COLUMN_NAME_NAME = "name"
    private const val MAX_PATH_LEN = 255

    val name = varchar(COLUMN_NAME_NAME, MAX_PATH_LEN).uniqueIndex()
}
