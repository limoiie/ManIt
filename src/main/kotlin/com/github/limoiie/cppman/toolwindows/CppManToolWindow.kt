package com.github.limoiie.cppman.toolwindows

import com.github.limoiie.cppman.services.MyApplicationService
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.structuralsearch.plugin.ui.TextFieldWithAutoCompletionWithBrowseButton
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import javax.swing.JPanel

class CppManToolWindow(project: Project, private val toolWindow: ToolWindow) {
    private val logger = logger<CppManToolWindow>()

    // Ui fields:

    private val toolWindowContent = JPanel()

    private val topPanel = JPanel()
    private val valueTxt = TextFieldWithAutoCompletionWithBrowseButton(project)
    private val manBtn = ComboBox(MyApplicationService.manEntries)

    private val manPagePanel = JBScrollPane()
    private val manPageTxt = JBTextArea()

    // State fields:

    private val man
        get() = manBtn.selectedItem as MyApplicationService.ManEntry

    init { initUi() }

    fun getContent(): JPanel = toolWindowContent

    private fun initUi() {
        logger.debug { "toolWindow: $toolWindow" }

        manBtn.selectedItem = null
        manBtn.addItemListener {
            logger.debug { "Man changed: $it" }
            if (it.stateChange == ItemEvent.SELECTED) {
                loadManCandidates()
            }
        }
        manBtn.selectedIndex = 0  // set default man

        valueTxt.childComponent.toolTipText = "Input the value to man with"
        valueTxt.childComponent.setPlaceholder("std::*")
        valueTxt.setButtonIcon(AllIcons.Actions.Refresh)
        valueTxt.addActionListener {
            service<MyApplicationService>().man(valueTxt.text, man.type)
        }

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

    fun updateUi(word: String, manPage: String? = null) {
        logger.debug { "updateUi with man value $word" }

        valueTxt.text = word
        manPageTxt.text = manPage?: ""

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
        logger.debug { "load candidates for $man"}

        service<MyApplicationService>().loadManCandidateWords(man.type) {
            valueTxt.setAutoCompletionItems(it)
        }
    }

}