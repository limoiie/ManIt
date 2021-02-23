package com.github.limoiie.cppman.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManSources : IntIdTable() {
    val path = varchar("path", 255).uniqueIndex()
}
