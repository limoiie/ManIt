package com.github.limoiie.cppman.database.dsl
import org.jetbrains.exposed.dao.id.IntIdTable

object ManSets : IntIdTable() {
    val name = text("name")
}
