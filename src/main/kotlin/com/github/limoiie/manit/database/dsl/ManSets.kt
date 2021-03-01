package com.github.limoiie.manit.database.dsl
import org.jetbrains.exposed.dao.id.IntIdTable

object ManSets : IntIdTable() {

    private const val COLUMN_NAME_NAME = "name"

    val name = text(COLUMN_NAME_NAME)
}
