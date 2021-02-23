package com.github.limoiie.cppman.services

import com.github.limoiie.cppman.database.dsl.ManFileSections
import com.github.limoiie.cppman.database.dsl.ManFiles
import com.github.limoiie.cppman.database.dsl.ManKeywords
import com.github.limoiie.cppman.database.dsl.ManSections
import com.github.limoiie.cppman.database.dsl.ManSources
import com.github.limoiie.cppman.services.impls.ManIndex
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path
import java.nio.file.Paths

class ManDbAppService {
    private val logger = logger<ManDbAppService>()

    private val databaseUri = "jdbc:sqlite::memory:"
    private val databaseDriver = "org.sqlite.JDBC"

    fun indexManRepo() {
        GlobalScope.launch {
            Database.connect(databaseUri, databaseDriver)
            transaction {
                prepareSchema()
                ManIndex.indexSources(manSourcePaths())
            }
        }
    }

    private fun prepareSchema() {
        SchemaUtils.create(ManFiles)
        SchemaUtils.create(ManSections)
        SchemaUtils.create(ManFileSections)
        SchemaUtils.create(ManKeywords)
        SchemaUtils.create(ManSources)
    }

    private fun manSourcePaths(): List<Path> {
        return listOf(
            "/Users/ligengwang/.opam/4.07.0/man",
            "/Users/ligengwang/.nvm/versions/node/v13.10.1/share/man",
            "/Users/ligengwang/anaconda3/share/man",
            "/usr/local/share/man",
            "/usr/share/man",
        ).map { Paths.get(it) }
    }
}
