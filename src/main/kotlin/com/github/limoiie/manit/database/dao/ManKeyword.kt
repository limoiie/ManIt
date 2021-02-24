package com.github.limoiie.manit.database.dao

import com.github.limoiie.manit.database.dsl.ManKeywords
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ManKeyword(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ManKeyword>(ManKeywords)
    var keyword by ManKeywords.keyword
    var file by ManFile referencedOn ManKeywords.file

    override fun toString(): String {
        return "ManKeyword[id:$id, k:$keyword]"
    }
}
