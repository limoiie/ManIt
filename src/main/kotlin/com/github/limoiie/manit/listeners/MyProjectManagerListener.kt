package com.github.limoiie.manit.listeners

import com.github.limoiie.manit.services.ManDbAppService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        service<ManDbAppService>()
    }
}
