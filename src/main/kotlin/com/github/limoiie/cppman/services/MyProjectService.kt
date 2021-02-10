package com.github.limoiie.cppman.services

import com.github.limoiie.cppman.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
