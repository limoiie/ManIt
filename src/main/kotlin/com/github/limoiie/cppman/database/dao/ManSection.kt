package com.github.limoiie.cppman.database.dao

import com.github.limoiie.cppman.database.dsl.ManSections
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ManSection(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ManSection>(ManSections)
    var name by ManSections.name

    override fun toString(): String {
        return "ManSection[n:$name]"
    }
}
