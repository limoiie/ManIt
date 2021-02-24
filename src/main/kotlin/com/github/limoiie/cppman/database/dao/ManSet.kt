package com.github.limoiie.cppman.database.dao

import com.github.limoiie.cppman.database.dsl.ManSetSources
import com.github.limoiie.cppman.database.dsl.ManSets
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ManSet(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ManSet>(ManSets)
    var name by ManSets.name
    val sources by ManSource via ManSetSources

    override fun toString(): String {
        return name
    }
}
