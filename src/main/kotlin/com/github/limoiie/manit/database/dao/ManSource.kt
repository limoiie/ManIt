package com.github.limoiie.manit.database.dao

import com.github.limoiie.manit.database.dsl.ManFiles
import com.github.limoiie.manit.database.dsl.ManSources
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ManSource(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ManSource>(ManSources)
    var path by ManSources.path
    val files by ManFile referrersOn ManFiles.manSource

    override fun toString(): String {
        return "ManSource[p:$path]"
    }
}
