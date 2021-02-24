package com.github.limoiie.cppman.database.dsl

import org.jetbrains.exposed.sql.Table

object ManSetSources : Table() {
    val manSet = reference("set", ManSets)
    val manSource = reference("source", ManSources)

    override val primaryKey = PrimaryKey(manSet, manSource)
}
