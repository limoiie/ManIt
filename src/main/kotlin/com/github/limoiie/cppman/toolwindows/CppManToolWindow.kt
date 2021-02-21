package com.github.limoiie.cppman.toolwindows

import com.github.limoiie.cppman.services.MyApplicationService
import com.github.limoiie.cppman.services.MyApplicationService.ManEntry
import com.github.limoiie.cppman.services.MyApplicationService.ManType
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
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.JPanel

class CppManToolWindow(project: Project, private val toolWindow: ToolWindow) {
    private val logger = logger<CppManToolWindow>()

    // Ui fields:

    private val toolWindowContent = JPanel()

    private val topPanel = JPanel()
    private val valueTxt = TextFieldWithAutoCompletionWithBrowseButton(project)

    private val configPanel = JPanel()
    private val manExeBox = ComboBox(MyApplicationService.manEntries)
    private val manSectionBox = ComboBox(MyApplicationService.manSections)

    private val manPagePanel = JBScrollPane()
    private val manPageTxt = JBTextArea()

    // State fields:

    private val man get() = manExeBox.selectedItem as ManEntry
    private val manSection get() = manSectionBox.selectedItem as String

    init { initUi() }

    fun getContent(): JPanel = toolWindowContent

    private fun initUi() {
        logger.debug { "toolWindow: $toolWindow" }

        // man config panel

        val actionUpdateConfigPanel = { it: ItemEvent ->
            logger.debug { "Man changed: $it" }
            if (it.stateChange == ItemEvent.SELECTED) {
                loadManCandidates()
                val item = it.item as ManEntry
                manSectionBox.isVisible =
                    item.type == ManType.StandardMan
            }
        }

        manExeBox.selectedItem = null
        manExeBox.addItemListener(actionUpdateConfigPanel)
        manExeBox.selectedIndex = 0  // set default man
        manExeBox.toolTipText = "Man executable"
        manSectionBox.addItemListener(actionUpdateConfigPanel)
        manSectionBox.selectedIndex = 0  // set default man
        manSectionBox.toolTipText = "Man section"

        manExeBox.setMinimumAndPreferredWidth(96)
        manSectionBox.setMinimumAndPreferredWidth(48)

        configPanel.layout = FlowLayout(FlowLayout.LEFT, 0, 0)
        configPanel.add(manExeBox)
        configPanel.add(manSectionBox)

        // man word panel

        valueTxt.childComponent.toolTipText = "The value to man with"
        valueTxt.childComponent.setPlaceholder("std::*")
        valueTxt.setButtonIcon(AllIcons.Actions.Refresh)
        valueTxt.addActionListener {
            service<MyApplicationService>().man(valueTxt.text, man.type, manSection)
        }

        // top panel = man config panel + man word panel

        topPanel.layout = BorderLayout()
        topPanel.add(valueTxt, BorderLayout.CENTER)
        topPanel.add(configPanel, BorderLayout.WEST)

        // man page panel

        manPageTxt.isEditable = false
        manPagePanel.setViewportView(manPageTxt)
        manPagePanel.autoscrolls = true

        // tool window content = top panel + man page panel

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

        val manType = man.type
        val manSec = manSection
        service<MyApplicationService>().loadManCandidateWords(manType, manSec) {
            ApplicationManager.getApplication().invokeLater {
                if (man.type == manType && manSection == manSec) { // update only if not outdated
                    valueTxt.setAutoCompletionItems(it)
                }
            }
        }
    }

}