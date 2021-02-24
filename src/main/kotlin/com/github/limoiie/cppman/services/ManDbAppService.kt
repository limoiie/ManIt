package com.github.limoiie.cppman.services

import com.github.limoiie.cppman.database.dao.ManSection
import com.github.limoiie.cppman.database.dao.ManSet
import com.github.limoiie.cppman.database.dao.ManSource
import com.github.limoiie.cppman.database.dsl.*
import com.github.limoiie.cppman.services.impls.ManFetch
import com.github.limoiie.cppman.services.impls.ManIndex
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

class ManDbAppService {
    private val logger = logger<ManDbAppService>()

    //    private val databaseUri = "jdbc:sqlite::memory:"
    private val databaseUri = "jdbc:sqlite:/Users/ligengwang/Downloads/test.db"
    private val databaseDriver = "org.sqlite.JDBC"

    private var service: ManDbService? = null

    private var indexingJob: Job? = null
    private val onIndexedListeners: MutableList<ManDbService.() -> Unit> = mutableListOf()

    private val isIndexing = AtomicBoolean(false)
    private val isIndexed = AtomicBoolean(false)

    init {
        Database.connect(databaseUri, databaseDriver)
        transaction {
            prepareSchema()
            isIndexed.set(ManIndex.isIndexed())
        }
        indexManRepo()
    }

    fun addOnIndexedListener(listener: ManDbService.() -> Unit) {
        synchronized(onIndexedListeners) {
            onIndexedListeners.add(listener)
            if (isIndexed.get() && !isIndexing.get()) {
                service!!.listener()
            }
        }
    }

    private fun indexManRepo() {
        if (!isIndexing.compareAndExchange(false, true)) {
            indexingJob?.cancel()
            indexingJob = GlobalScope.launch {
                try {
                    if (!isIndexed.get()) {
                        transaction {
                            ManIndex.indexSources(manSourcePaths())
                        }
                    }
                    notifyIndexed()
                    isIndexed.set(true)
                } finally {
                    isIndexing.set(false)
                }
            }
        }
    }

    private fun notifyIndexed() {
        service = ManDbService()
        synchronized(onIndexedListeners) {
            for (listener in onIndexedListeners) {
                service?.listener()
            }
        }
    }

    private fun prepareSchema() {
        SchemaUtils.create(ManFiles)
        SchemaUtils.create(ManSections)
        SchemaUtils.create(ManFileSections)
        SchemaUtils.create(ManKeywords)
        SchemaUtils.create(ManSources)
        SchemaUtils.create(ManSets)
        SchemaUtils.create(ManSetSources)
    }

    // todo - get by manpath or something else
    private fun manSourcePaths(): List<Path> {
        return listOf(
            "/Users/ligengwang/.opam/4.07.0/man",
            "/Users/ligengwang/.nvm/versions/node/v13.10.1/share/man",
            "/Users/ligengwang/anaconda3/share/man",
            "/usr/local/share/man",
            "/usr/share/man",
            "/Library/TeX/texbin/man",
            "/opt/X11/share/man",
            "/Library/Apple/usr/share/man",
            "/Library/Developer/CommandLineTools/usr/share/man",
        ).map { Paths.get(it) }
    }

    class ManDbService {
        var allManSections: List<ManSection> = listOf()
            private set

        var allManSets: List<ManSet> = listOf()
            private set

        private var allManSources: List<ManSource> = listOf()

        private var jobGetKeywords: Job? = null

        init {
            transaction {
                allManSections = ManFetch.getAllManSections()
                allManSources = ManFetch.getAllManSources()
                allManSets = ManFetch.getAllManSets()
            }
        }

        fun keywords(
            sections: Collection<ManSection>?, manSet: ManSet,
            onLoaded: (Collection<String>) -> Unit
        ): Job? {
            jobGetKeywords?.cancel()
            jobGetKeywords = GlobalScope.launch {
                val keywords = transaction {
                    ManFetch.getKeywords(
                        sections ?: allManSections,
                        manSet.sources.toList()
                    )
                }
                onLoaded(keywords)
            }
            return jobGetKeywords
        }

        fun manpage(keyword: String, onLoaded: (String?) -> Unit) {
            val manFile = transaction {
                ManFetch.getManFileByKeyword(keyword)
            }
            onLoaded(manFile?.path)
        }
    }
}
