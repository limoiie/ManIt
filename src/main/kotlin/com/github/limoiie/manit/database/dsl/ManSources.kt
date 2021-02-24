package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManSources : IntIdTable() {
    val path = varchar("path", 255).uniqueIndex()
}
