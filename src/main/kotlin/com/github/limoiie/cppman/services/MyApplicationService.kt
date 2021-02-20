package com.github.limoiie.cppman.services

import com.github.limoiie.cppman.MyBundle
import com.github.limoiie.cppman.runCommand
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.debug
import kotlinx.coroutines.*
import kotlinx.coroutines.time.withTimeout

class MyApplicationService {

    private val logger = logger<MyApplicationService>()
    private var preJob: Job? = null

    init {
        println(MyBundle.message("applicationService"))
    }

    fun man(qualifiedNames: String) {
        if (preJob != null) {
            preJob?.cancel()
        }
        preJob = GlobalScope.launch {
            withTimeoutOrNull(10_000) {
                if (!isActive) return@withTimeoutOrNull
                val page = "cppman $qualifiedNames".runCommand()
                logger.debug {
                    page?: "Failed to run cppman"
                }
            }
        }
    }

}
