package com.github.limoiie.cppman.toolwindows

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class CppManToolWindowFactory : ToolWindowFactory {
    private val logger = logger<CppManToolWindowFactory>()

    companion object {
        var cppManToolWindow: CppManToolWindow? = null
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        logger.info("createToolWindowContent")

        cppManToolWindow = CppManToolWindow(toolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(cppManToolWindow!!.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}