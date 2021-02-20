package com.github.limoiie.cppman.toolwindows

import com.github.limoiie.cppman.services.MyApplicationService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JButton
import javax.swing.JPanel

class CppManToolWindow(project: Project, private val toolWindow: ToolWindow) {
    private val logger = logger<CppManToolWindow>()

    private val inputTxt = TextFieldWithAutoCompletion.create(
        project, listOf(), true, null)

    private val manBtn = JButton("Man")

    private val toolWindowContent = JPanel()
    private val topPanel = JPanel()

    private val manPagePanel = JBScrollPane()
    private val manPageTxt = JBTextArea()

    init {
        manBtn.addActionListener {
            service<MyApplicationService>().man(inputTxt.text, "cppman")
        }
        inputTxt.toolTipText = "Input the value to man with"
        inputTxt.setPlaceholder("std::*")
        inputTxt.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent?) {
                logger.debug { "key event: $e" }
            }
        })
        loadManCandidates()

        topPanel.layout = BorderLayout()
        topPanel.add(inputTxt, BorderLayout.CENTER)
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
        inputTxt.text = word
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
        service<MyApplicationService>().loadManCandidateWords {
            inputTxt.setVariants(it)
        }
    }

}