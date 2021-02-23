package com.github.limoiie.cppman.services

import com.intellij.openapi.components.service

class ManAppService {
    companion object {
        val instance: ManAppService
            get() = service()
    }

    fun indexMan() {
        // todo : index man if not in daemon thread
    }

    fun manPage(word: String, section: String?) {
        // todo 1 : get the man page by [word] and [section]
        // todo 2 : render the man page
    }

    fun candidates(section: String?) {
        // todo : get the man page by [word] and [section]
    }
}
