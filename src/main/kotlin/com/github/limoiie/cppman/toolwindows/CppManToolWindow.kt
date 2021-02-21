package com.github.limoiie.cppman.toolwindows

import com.github.limoiie.cppman.services.MyApplicationService
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.structuralsearch.plugin.ui.TextFieldWithAutoCompletionWithBrowseButton
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

class CppManToolWindow(project: Project, private val toolWindow: ToolWindow) {
    private val logger = logger<CppManToolWindow>()

    private val toolWindowContent = JPanel()

    private val topPanel = JPanel()
    private val valueTxt = TextFieldWithAutoCompletionWithBrowseButton(project)
    private val manBtn = JButton()

    private val manPagePanel = JBScrollPane()
    private val manPageTxt = JBTextArea()

    init {
        manBtn.text = "Man"
        manBtn.addActionListener {
//            service<MyApplicationService>().man(inputTxt.text, "cppman")
        }
        valueTxt.childComponent.toolTipText = "Input the value to man with"
        valueTxt.childComponent.setPlaceholder("std::*")
        valueTxt.setButtonIcon(AllIcons.Actions.Refresh)
        valueTxt.addActionListener {
            service<MyApplicationService>().man(valueTxt.text, "cppman")
        }
        loadManCandidates()

        topPanel.layout = BorderLayout()
        topPanel.add(valueTxt, BorderLayout.CENTER)
        topPanel.add(manBtn, BorderLayout.WEST)

        manPageTxt.isEditable = false
        manPagePanel.setViewportView(manPageTxt)
        manPagePanel.autoscrolls = true

        toolWindowContent.layout = BorderLayout()
        toolWindowContent.add(topPanel, BorderLayout.NORTH)
        toolWindowContent.add(manPagePanel, BorderLayout.CENTER)
    }

    fun getContent(): JPanel = toolWindowContent

    fun updateUi(word: String, manPage: String? = null) {
        valueTxt.text = word
        manPageTxt.text = manPage?: ""

        logger.debug { "updateUi with man value $word" }

        // reset the scrollBar of manPageLayout after update the text
        GlobalScope.launch {  // call invokeLater in other thread rather than the ui one
            delay(100)
            // so that the manPageLayout could be updated before resetting its ScrollBar
            ApplicationManager.getApplication().invokeLater {
                manPagePanel.verticalScrollBar.value = 0
                manPagePanel.horizontalScrollBar.value = 0
            }
        }
    }

    private fun loadManCandidates() {
        service<MyApplicationService>().loadManCandidateWords {
            valueTxt.setAutoCompletionItems(it)
        }
    }

}