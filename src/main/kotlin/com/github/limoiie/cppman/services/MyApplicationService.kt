package com.github.limoiie.cppman.services

import com.github.limoiie.cppman.MyBundle
import com.github.limoiie.cppman.mantool.CppMan
import com.github.limoiie.cppman.mantool.Man
import com.github.limoiie.cppman.toolwindows.CppManToolWindowFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.*

class MyApplicationService {

    enum class ManType {
        StandardMan,
        CppMan
    }

    data class ManEntry(val type: ManType, val desc: String) {
        override fun toString() = desc
    }

    companion object {
        val manEntries = arrayOf(
            ManEntry(ManType.StandardMan, "Man"),
            ManEntry(ManType.CppMan, "CppMan")
        )

        const val allSections = "*"

        val manSections = arrayOf(
            allSections, "1", "2", "3", "4", "5", "6", "7", "8", "9", "l", "n"
        )

    }

    private val men = mapOf(
        ManType.StandardMan to Man(),
        ManType.CppMan to CppMan(),
    )

    private val logger = logger<MyApplicationService>()
    private var preJob: Job? = null
    private var page: String? = null

    init {
        println(MyBundle.message("applicationService"))
    }

    /**
     * Invoke [man] on [word] in other thread and show the result in tool window
     */
    fun man(word: String, man: ManType, section: String? = null) {
        if (preJob?.isCancelled == true) {
            preJob?.cancel()
        }
        preJob = GlobalScope.launch {
            withTimeoutOrNull(10_000) {
                if (!isActive) return@withTimeoutOrNull
                page = men[man]?.manPage(word, section)

                if (!isActive) return@withTimeoutOrNull
                ApplicationManager.getApplication().invokeLater {
                    CppManToolWindowFactory.cppManToolWindow?.updateUi(word, page)
                }
            }
        }
    }

    /**
     * Invoke [man] in other thread to get a list of candidates and load them with [onLoaded]
     */
    fun loadManCandidateWords(man: ManType, section: String? = null, onLoaded: (Collection<String>) -> Unit) {
        GlobalScope.launch {
            val candidates = men[man]?.candidates(section)
            if (candidates != null) {
                onLoaded(candidates)
            }
        }
    }

}
