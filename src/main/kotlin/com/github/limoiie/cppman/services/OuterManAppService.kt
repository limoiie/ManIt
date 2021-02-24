package com.github.limoiie.cppman.services

import com.github.limoiie.cppman.MyBundle
import com.github.limoiie.cppman.mantool.CppMan
import com.github.limoiie.cppman.mantool.Man
import com.github.limoiie.cppman.mantool.MyMan
import com.github.limoiie.cppman.toolwindows.CppManToolWindowFactory
import com.github.limoiie.cppman.toolwindows.manSectionTips
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.launch

class OuterManAppService {
    enum class ManType {
        StandardMan,
        CppMan,
        MyMan,
    }

    data class ManEntry(val type: ManType, val desc: String) {
        override fun toString() = desc
    }

    companion object {
        const val MAN_TIMEOUT: Long = 10_000

        val manEntries = arrayOf(
            ManEntry(ManType.MyMan, "MyMan"),
            ManEntry(ManType.StandardMan, "Man"),
            ManEntry(ManType.CppMan, "CppMan"),
        )

        const val allSections = "*"

        val manSections = manSectionTips
    }

    private val men = mapOf(
        ManType.MyMan to MyMan(),
        ManType.StandardMan to Man(),
        ManType.CppMan to CppMan(),
    )

    private val logger = logger<OuterManAppService>()
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
            withTimeoutOrNull(MAN_TIMEOUT) {
                if (!isActive) return@withTimeoutOrNull
                page = men[man]?.manPage(word, section)

                if (!isActive) return@withTimeoutOrNull
                ApplicationManager.getApplication().invokeLater {
                    CppManToolWindowFactory.cppManToolWindow?.showManPage(word, page)
                }
            }
        }
    }

    /**
     * Invoke [man] in other thread to get a list of candidates and load them with [onLoaded]
     */
    fun loadManCandidateWords(man: ManType, section: String? = null, onLoaded: (Collection<String>) -> Unit) {
        logger.debug { "load man completion list for $man in $section" }

        GlobalScope.launch {
            val candidates = men[man]?.candidates(section)
            if (candidates != null) {
                onLoaded(candidates)
            }
        }
    }
}
