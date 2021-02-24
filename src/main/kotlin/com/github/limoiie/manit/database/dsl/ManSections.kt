package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManSections : IntIdTable() {
    val name = varchar("name", 255).uniqueIndex()
}
