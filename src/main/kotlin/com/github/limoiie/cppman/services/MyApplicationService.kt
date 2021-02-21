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

        private val men = mapOf(
            ManType.StandardMan to Man(),
            ManType.CppMan to CppMan(),
        )

    }

    private val logger = logger<MyApplicationService>()
    private var preJob: Job? = null
    private var page: String? = null

    init {
        println(MyBundle.message("applicationService"))
    }

    /**
     * Invoke [man] on [word] in other thread and show the result in tool window
     */
    fun man(word: String, man: ManType) {
        if (preJob?.isCancelled == true) {
            preJob?.cancel()
        }
        preJob = GlobalScope.launch {
            withTimeoutOrNull(10_000) {
                if (!isActive) return@withTimeoutOrNull
                page = men[man]?.manPage(word)

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
    fun loadManCandidateWords(man: ManType, onLoaded: (Collection<String>) -> Unit) {
        GlobalScope.launch {
            val candidates = men[man]?.candidates()
            if (candidates != null) {
                onLoaded(candidates)
            }
        }
    }

}
