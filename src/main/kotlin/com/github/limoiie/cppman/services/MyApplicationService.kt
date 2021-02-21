package com.github.limoiie.cppman.services

import com.github.limoiie.cppman.MyBundle
import com.github.limoiie.cppman.runCommand
import com.github.limoiie.cppman.toolwindows.CppManToolWindowFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.*

class MyApplicationService {

    private val logger = logger<MyApplicationService>()
    private var preJob: Job? = null
    private var page: String? = null

    init {
        println(MyBundle.message("applicationService"))
    }

    /**
     * Invoke [man] on [word] in other thread and show the result in tool window
     */
    fun man(word: String, man: String = "cppman") {
        if (preJob?.isCancelled == true) {
            preJob?.cancel()
        }
        preJob = GlobalScope.launch {
            withTimeoutOrNull(10_000) {
                if (!isActive) return@withTimeoutOrNull
                page = "$man $word".runCommand()
                logger.debug { page?: "Failed to run $man" }

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
    fun loadManCandidateWords(man: String = "cppman", onLoaded: (Collection<String>) -> Unit) {
        GlobalScope.launch {
            val list = "$man -f std::".runCommand()
            val candidates = list?.splitToSequence('\n')
                ?.map { it.split('-').first().trim() }
            if (candidates != null) {
                onLoaded(candidates.toList())
            }
        }
    }

}
