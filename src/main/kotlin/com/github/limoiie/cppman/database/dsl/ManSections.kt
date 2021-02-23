package com.github.limoiie.cppman.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManSections : IntIdTable() {
    val name = varchar("name", 255).uniqueIndex()
}
