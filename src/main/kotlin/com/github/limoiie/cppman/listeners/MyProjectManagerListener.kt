package com.github.limoiie.cppman.listeners

import com.github.limoiie.cppman.services.ManDbAppService
import com.github.limoiie.cppman.services.MyProjectService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<MyProjectService>()
        service<ManDbAppService>()
    }
}
