package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.sql.Table

object ManSetSources : Table() {
    private const val COLUMN_NAME_SET = "set"
    private const val COLUMN_NAME_SOURCE = "source"

    val manSet = reference(COLUMN_NAME_SET, ManSets)
    val manSource = reference(COLUMN_NAME_SOURCE, ManSources)

    override val primaryKey = PrimaryKey(manSet, manSource)
}
