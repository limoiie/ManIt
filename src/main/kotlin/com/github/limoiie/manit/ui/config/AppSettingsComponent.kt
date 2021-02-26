package com.github.limoiie.manit.ui.config

import com.github.limoiie.manit.ui.config.tablemodels.ManSetTableModel
import com.github.limoiie.manit.ui.config.tablemodels.ManSourceTableModel
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.event.ListSelectionEvent

class AppSettingsComponent {
    private val logger = logger<AppSettingsComponent>()

    private var myMainPanel: JPanel? = null
    private val myUserNameText = JBTextField()
    private val myIdeaUserStatus = JBCheckBox("Do you use IntelliJ IDEA? ")

    private val containerPanel = JPanel(BorderLayout(fragmentHGap, 0))

    private val manSetTableModel = ManSetTableModel()
    private val manSourceTableModel = ManSourceTableModel()

    private val manSetTable = JBTable(manSetTableModel)
    private val manSourceTable = JBTable(manSourceTableModel)

    companion object {
        const val fragmentHGap = 8
        const val checkboxColWidth = 24
        const val gapBetweenTableAndBar = -2
    }

    init {
        logger.debug { "open setting" }

        manSetTable.selectionModel.addListSelectionListener {
            onManSetTableItemSelected(it)
        }

        val leftFrame = createTableView(manSetTable)
        val mainFrame = createTableView(manSourceTable)

        containerPanel.add(leftFrame, BorderLayout.WEST)
        containerPanel.add(mainFrame, BorderLayout.CENTER)

        val col = manSourceTable.columnModel.getColumn(0)
        col.cellEditor = JBTable.createBooleanEditor()
        col.cellRenderer = BooleanTableCellRenderer()
        col.preferredWidth = checkboxColWidth
        col.maxWidth = checkboxColWidth

        myMainPanel = FormBuilder.createFormBuilder()
            .addComponentFillVertically(containerPanel, 0)
            .addComponent(myIdeaUserStatus, 1)
            .panel
    }

    fun getPanel(): JPanel? {
        return myMainPanel
    }

    fun getPreferredFocusedComponent(): JComponent {
        return myUserNameText
    }

    fun getUserNameText(): String {
        return myUserNameText.text
    }

    fun setUserNameText(newText: String?) {
        myUserNameText.text = newText
    }

    fun getIdeaUserStatus(): Boolean {
        return myIdeaUserStatus.isSelected
    }

    fun setIdeaUserStatus(newStatus: Boolean) {
        myIdeaUserStatus.isSelected = newStatus
    }

    private fun onManSetTableItemSelected(e: ListSelectionEvent) {
        if (e.valueIsAdjusting) return
        manSourceTableModel.bindManSet(
            manSetTableModel.getRawData(e.firstIndex)
        )
    }

    private fun createTableView(table: JTable): JPanel {
        val toolbar = ToolbarDecorator.createDecorator(table)
            .createPanel()

        val panel = JBScrollPane()
        panel.setViewportView(table)

        val frame = JPanel(BorderLayout(0, gapBetweenTableAndBar))
        frame.add(panel, BorderLayout.CENTER)
        frame.add(toolbar, BorderLayout.SOUTH)

        return frame
    }
}
