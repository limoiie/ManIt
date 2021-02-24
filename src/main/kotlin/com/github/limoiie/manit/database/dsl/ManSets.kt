package com.github.limoiie.manit.database.dsl
import org.jetbrains.exposed.dao.id.IntIdTable

object ManSets : IntIdTable() {
    val name = text("name")
}
