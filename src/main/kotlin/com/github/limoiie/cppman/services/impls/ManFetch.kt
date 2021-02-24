package com.github.limoiie.cppman.services.impls

import com.github.limoiie.cppman.database.dao.ManFile
import com.github.limoiie.cppman.database.dao.ManKeyword
import com.github.limoiie.cppman.database.dao.ManSection
import com.github.limoiie.cppman.database.dao.ManSet
import com.github.limoiie.cppman.database.dao.ManSource
import com.github.limoiie.cppman.database.dsl.ManFileSections
import com.github.limoiie.cppman.database.dsl.ManFiles
import com.github.limoiie.cppman.database.dsl.ManKeywords
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select

object ManFetch {
    fun getAllManSections(): List<ManSection> {
        return ManSection.all().toList()
    }

    fun getAllManSources(): List<ManSource> {
        return ManSource.all().toList()
    }

    fun getAllManSets(): List<ManSet> {
        return ManSet.all().toList()
    }

    fun getKeywords(sections: Collection<ManSection>, sources: Collection<ManSource>): List<String> {
        val manSections = sections.map(ManSection::id)
        val manSources = sources.map(ManSource::id)

        return ManKeywords
            .leftJoin(ManFileSections, { file }, { file })
            .leftJoin(ManFiles, { ManKeywords.file }, { id })
            .slice(ManKeywords.keyword, ManFileSections.section, ManFiles.manSource)
            .select {
                (ManFileSections.section inList manSections) and
                        (ManFiles.manSource inList manSources)
            }
            .map { it[ManKeywords.keyword] }
    }

    fun getManFileByKeyword(keyword: String): ManFile? {
        val manKeyword = ManKeyword
            .find { (ManKeywords.keyword eq keyword) }
            .firstOrNull()
        return manKeyword?.file
    }

}