package com.github.limoiie.cppman.database.dao

import com.github.limoiie.cppman.database.dsl.ManFileSections
import com.github.limoiie.cppman.database.dsl.ManFiles
import com.github.limoiie.cppman.database.dsl.ManKeywords
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ManFile(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ManFile>(ManFiles)

    var path by ManFiles.path
    var src by ManSource referencedOn ManFiles.manSource
    val sections by ManSection via ManFileSections
    val keywords by ManKeyword referrersOn ManKeywords.file

    override fun toString(): String {
        return "ManFile[p:$path]"
    }
}
