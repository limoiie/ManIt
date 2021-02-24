package com.github.limoiie.manit.database.dao

import com.github.limoiie.manit.database.dsl.ManSections
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ManSection(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ManSection>(ManSections)
    var name by ManSections.name

    override fun toString(): String {
        return name
    }
}
