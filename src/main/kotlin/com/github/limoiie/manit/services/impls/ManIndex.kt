package com.github.limoiie.manit.services.impls

import com.github.limoiie.manit.database.dao.ManFile
import com.github.limoiie.manit.database.dao.ManKeyword
import com.github.limoiie.manit.database.dao.ManSection
import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.database.dao.ManSource
import com.github.limoiie.manit.database.dsl.ManFileSections
import com.github.limoiie.manit.database.dsl.ManSetSources
import com.github.limoiie.manit.runCommand
import com.github.limoiie.manit.services.impls.indexing.ManFileEntry
import com.github.limoiie.manit.services.impls.indexing.parseManFileEntry
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import org.jetbrains.exposed.sql.insert
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object ManIndex {
    private const val manPrefix = "man"
    private const val nameOfAllManSet = "All"
    private val logger = logger<ManIndex>()

    /**
     * Index man source which has not been indexed in sequentially.
     *
     * A man source is a directory which contains sub directories like 'man1', 'man2', etc.
     */
    fun indexSources() {
        initDbIfEmpty()

        ManSource.all()
            .filterNot(ManSource::indexed)
            .forEach(ManIndex::indexOneSource)

        logger.debug {
            val c = ManFile.count()
            "ManFile Count: $c"
        }
    }

    /**
     * Initialize the database with default values.
     * - if table [ManSource] is empty, initialize with the man sources from 'manpath'
     * - if table [ManSet] is empty, add a set named 'All' which contains all the sources.
     */
    private fun initDbIfEmpty() {
        if (ManSource.count() == 0L) {
            logger.debug { "Table ManSource is empty, init it" }
            for (manSourcePath in manSourcePaths()) {
                ManSource.new {
                    path = manSourcePath
                    indexed = false
                }
            }
        }

        if (ManSet.count() == 0L) {
            logger.debug { "Table ManSet is empty, init it" }
            val set = ManSet.new {
                name = nameOfAllManSet
            }
            ManSource.all().forEach { src ->
                ManSetSources.insert {
                    it[manSet] = set.id
                    it[manSource] = src.id
                }
            }
        }
    }

    private fun manSourcePaths(): List<String> {
        return "manpath".runCommand()?.split(':') ?: listOf(
            "/usr/share/man",
            "/usr/local/share/man"
        )
    }

    /**
     * Index the given source [manSource]
     */
    private fun indexOneSource(manSource: ManSource) {
        logger.debug { "Indexing source: ${manSource.path}" }

        val manSourcePath = Paths.get(manSource.path)
        val fnParseManFileEntry = { (section, manFilePath): Pair<String, Path> ->
            parseManFileEntry(manSourcePath, section, manFilePath)
        }

        val manEntryIndex = ManEntriesIndex(manSource)
        enumerateManFiles(manSourcePath)
            .map(fnParseManFileEntry)
            .filterNotNull()
            .forEach { entry ->
                manEntryIndex.index(entry)
            }

        manSource.indexed = true
    }

    /**
     * Enumerate man files under [manSourcePath].
     *
     * Each item in the returned stream is a pair of the [ManSection] and the path
     * of the man file
     */
    private fun enumerateManFiles(manSourcePath: Path): Sequence<Pair<String, Path>> {
        val fnIsManSecDir = { path: Path ->
            Files.isDirectory(path) &&
                    path.fileName.toString().startsWith(manPrefix)
        }
        return Files.walk(manSourcePath, 1)
            .iterator().asSequence().drop(1) // drop itself
            .filter(fnIsManSecDir)
            .flatMap { manSecDirPath -> // mann
                val section = manSecDirPath.fileName.toString().substring(manPrefix.length)
                Files.walk(manSecDirPath)
                    .iterator().asSequence().drop(1) // drop itself
                    .filter { Files.isRegularFile(it) }
                    .map { Pair(section, it) }
            }
    }

    private class ManEntriesIndex(private val manSource: ManSource) {
        private val manSectionsCached = ManSection.all()
            .associateBy(ManSection::name).toMutableMap()

        fun index(entry: ManFileEntry) {
            val manFile = ManFile.new {
                path = entry.manFile.toString()
                src = manSource
            }

            val manSection = newManSectionIfNotExist(entry.sections.first())

            ManFileSections.insert {
                it[this.file] = manFile.id
                it[this.section] = manSection.id
            }

            for (kw in entry.keywords) {
                ManKeyword.new {
                    keyword = kw
                    file = manFile
                }
            }
        }

        private fun newManSectionIfNotExist(section: String): ManSection {
            if (section !in manSectionsCached) {
                manSectionsCached[section] = ManSection.new {
                    name = section
                }
            }
            return manSectionsCached[section]!!
        }
    }
}
