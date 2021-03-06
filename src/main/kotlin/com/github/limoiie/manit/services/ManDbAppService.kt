package com.github.limoiie.manit.services

import com.github.limoiie.manit.database.dao.ManSection
import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.database.dao.ManSource
import com.github.limoiie.manit.database.dsl.ManFileSections
import com.github.limoiie.manit.database.dsl.ManFiles
import com.github.limoiie.manit.database.dsl.ManKeywords
import com.github.limoiie.manit.database.dsl.ManSections
import com.github.limoiie.manit.database.dsl.ManSetSources
import com.github.limoiie.manit.database.dsl.ManSets
import com.github.limoiie.manit.database.dsl.ManSources
import com.github.limoiie.manit.database.helper.ManFetch
import com.github.limoiie.manit.mantool.index.ManIndex
import com.github.limoiie.manit.mantool.loader.ManPageRawLoader
import com.github.limoiie.manit.mantool.renders.DefaultLineRender
import com.github.limoiie.manit.mantool.renders.DefaultManpageRender
import com.github.limoiie.manit.mantool.renders.DefaultSectionRender
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.jetbrains.rd.util.Maybe
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

class ManDbAppService {
    private val logger = logger<ManDbAppService>()

    // todo - use roaming folder
    private val databaseUri = "jdbc:sqlite:${PathManager.getOptionsPath()}/ManItIndex.db"
    private val databaseDriver = "org.sqlite.JDBC"

    private var serviceImpl: ManDbService? = null

    private val indexing = AtomicBoolean(false)
    private val indexLock = ReentrantReadWriteLock()

    private var indexingJob: Job? = null

    val service: BehaviorSubject<Maybe<ManDbService>> =
        BehaviorSubject.createDefault(Maybe.None)

    init {
        Database.connect(databaseUri, databaseDriver)
        transaction {
            prepareSchema()
        }
        indexManRepo()
    }

    // todo - show the indexing progress
    fun indexManRepo() {
        if (indexLock.isWriteLocked) return
        if (!indexing.compareAndExchange(false, true)) {
            indexingJob = GlobalScope.launch {
                fireUpdated(false)

                logger.debug { "Start indexing ManRepo" }
                indexLock.writeLock().withLock {
                    transaction {
                        ManIndex.indexSources()
                    }
                }

                fireUpdated()
                indexing.set(false)
            }
        }
    }

    fun <R> untilReady(action: ManDbService.() -> R): R = runBlocking {
        while (indexing.get()) delay(200)
        indexLock.readLock().withLock {
            serviceImpl!!.action()
        }
    }

    fun whenReady(action: ManDbService.() -> Unit): Job {
        return GlobalScope.launch {
            while (indexing.get()) delay(200)
            indexLock.readLock().withLock {
                if (isActive) {
                    serviceImpl!!.action()
                }
            }
        }
    }

    private fun fireUpdated(finished: Boolean = true) {
        indexLock.readLock().withLock {
            if (finished) {
                serviceImpl = ManDbService(this)
                service.onNext(Maybe.Just(serviceImpl!!))
            } else {
                service.onNext(Maybe.None)
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

    class ManDbService(private val service: ManDbAppService) {
        var allManSections: List<ManSection> = listOf()
            private set

        var allManSets: List<ManSet> = listOf()
            private set

        var allManSources: List<ManSource> = listOf()
            private set

        private var jobGetKeywords: Job? = null
        private var jobGetManpage: Job? = null

        private val render = DefaultManpageRender(
            DefaultSectionRender(DefaultLineRender())
        )

        init {
            transaction {
                allManSections = ManFetch.getAllManSections()
                allManSources = ManFetch.getAllManSources()
                allManSets = ManFetch.getAllManSets()
            }
        }

        fun keywords(
            manSet: ManSet,
            sections: Collection<ManSection>?,
            onLoaded: (Collection<String>) -> Unit
        ): Job? {
            jobGetKeywords?.cancel()
            jobGetKeywords = GlobalScope.launch {
                val keywords = transaction {
                    ManFetch.getKeywords(
                        manSet, sections ?: allManSections
                    )
                }
                onLoaded(keywords)
            }
            return jobGetKeywords
        }

        fun manpage(
            keyword: String,
            manSet: ManSet,
            manSections: Collection<ManSection>?,
            onLoaded: (String?) -> Unit
        ): Job? {
            jobGetManpage?.cancel()
            jobGetManpage = GlobalScope.launch {
                val manFile = transaction {
                    ManFetch.getManFile(
                        keyword, manSet, manSections ?: allManSections
                    )
                }
                if (manFile != null) {
                    val rawPage = ManPageRawLoader.loadManPage(manFile.path)
                    val renderedPage = render.render(rawPage)
                    onLoaded(manFile.path + renderedPage)
                }
            }
            return jobGetManpage
        }

        fun sections(manSet: ManSet): List<ManSection> {
            return transaction {
                ManFetch.getSections(manSet)
            }
        }

        fun doUpdate(statements: () -> Unit) {
            transaction {
                statements()
            }
            fireDbUpdated()
        }

        fun <R> doFind(statements: () -> R): R {
            return transaction {
                statements()
            }
        }

        private fun fireDbUpdated() {
            service.fireUpdated()
        }
    }
}
