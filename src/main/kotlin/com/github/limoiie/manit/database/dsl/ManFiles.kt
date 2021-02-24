package com.github.limoiie.manit.database.dsl

import org.jetbrains.exposed.dao.id.IntIdTable

object ManFiles : IntIdTable() {
    val path = varchar("path", 255)
    val manSource = reference("source", ManSources)
}
