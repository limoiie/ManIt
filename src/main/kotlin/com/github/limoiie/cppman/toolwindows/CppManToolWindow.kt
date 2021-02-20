package com.github.limoiie.cppman.toolwindows

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBTextArea
import java.time.LocalDateTime
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

class CppManToolWindow(private val toolWindow: ToolWindow) {
    private val timeTxt: JTextField = JTextField()

    private val refreshBtn = JButton()
    private val hiddenBtn = JButton()

    private val toolWindowContent = JPanel()
    private val timePanel = JPanel()

    private val manPagePanel = JBTextArea()

    init {
        timeTxt.text = "null"
        refreshBtn.text = "Refresh"
        hiddenBtn.text = "Hidden"

        refreshBtn.addActionListener { updateUi() }
        hiddenBtn.addActionListener { toolWindow.hide() }

        timePanel.add(refreshBtn)
        timePanel.add(hiddenBtn)
        timePanel.add(timeTxt)

        toolWindowContent.add(timePanel)
        toolWindowContent.add(manPagePanel)
    }

    fun getContent(): JPanel = toolWindowContent

    fun updateUi(manPage: String? = null) {
        timeTxt.text = LocalDateTime.now().toString()
        manPagePanel.text = manPage?: ""
    }

}