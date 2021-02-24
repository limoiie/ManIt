package com.github.limoiie.manit.database.dsl
import org.jetbrains.exposed.dao.id.IntIdTable

private const val COLUMN_NAME_NAME = "name"

object ManSets : IntIdTable() {
    val name = text(COLUMN_NAME_NAME)
}
