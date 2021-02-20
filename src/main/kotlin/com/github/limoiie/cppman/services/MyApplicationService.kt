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

    fun man(qualifiedNames: String) {
        if (preJob?.isCancelled == true) {
            preJob?.cancel()
        }
        preJob = GlobalScope.launch {
            withTimeoutOrNull(10_000) {
                if (!isActive) return@withTimeoutOrNull
                page = "cppman $qualifiedNames".runCommand()
                logger.debug {
                    page?: "Failed to run cppman"
                }
                if (!isActive) return@withTimeoutOrNull
                ApplicationManager.getApplication().invokeLater {
                    CppManToolWindowFactory.cppManToolWindow?.updateUi(page)
                }
            }
        }
    }

}
