package com.github.limoiie.cppman.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManKeywords : IntIdTable() {
    val keyword = text("keyword")
    val file = reference("file", ManFiles)
}
