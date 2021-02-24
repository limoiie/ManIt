package com.github.limoiie.cppman.services.impls

import com.github.limoiie.cppman.database.dao.ManFile
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

    fun getKeywords(manSet: ManSet, sections: Collection<ManSection>): List<String> {
        val manSections = sections.map(ManSection::id)
        val manSources = manSet.sources.map(ManSource::id)

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

    fun getManFile(keyword: String, manSet: ManSet, sections: Collection<ManSection>): ManFile? {
        val manSections = sections.map(ManSection::id)
        val manSources = manSet.sources.map(ManSource::id)

        val manFileId = ManKeywords
            .leftJoin(ManFileSections, { file }, { file })
            .leftJoin(ManFiles, { ManKeywords.file }, { id })
            .slice(ManFiles.id)
            .select {
                (ManKeywords.keyword eq keyword) and
                        (ManFileSections.section inList manSections) and
                        (ManFiles.manSource inList manSources)
            }
            .limit(1)
            .map { it[ManFiles.id] }
            .firstOrNull()?.value ?: -1

        return ManFile.findById(manFileId)
    }
}
