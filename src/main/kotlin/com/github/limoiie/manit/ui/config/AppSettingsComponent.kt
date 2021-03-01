package com.github.limoiie.manit.ui.config

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.ui.config.tablemodels.DbTableModel.DataWrapper
import com.github.limoiie.manit.ui.config.tablemodels.ManSetTableModel
import com.github.limoiie.manit.ui.config.tablemodels.ManSourcePageTableModel
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import com.jetbrains.rd.util.Maybe
import io.reactivex.rxjava3.subjects.Subject
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.event.TableModelEvent

// todo - block the settings while indexing
class AppSettingsComponent(
    private val manSetTableModel: ManSetTableModel,
    manSourceTableModel: ManSourcePageTableModel,
    private val selectedManSet: Subject<Maybe<DataWrapper<ManSet>>>
) {
    private val logger = logger<AppSettingsComponent>()

    private var myMainPanel: JPanel? = null

    private val containerPanel = JPanel(BorderLayout(fragmentHGap, 0))

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
            if (it.valueIsAdjusting) return@addListSelectionListener
            val selected = selectedManSet()
            selectedManSet.onNext(
                if (selected == null) Maybe.None else Maybe.Just(selected)
            )
        }
        manSourceTableModel.addTableModelListener {
            if (it.type == TableModelEvent.INSERT) {
                // focus the source path cell after inserting a new row
                manSourceTable.editCellAt(it.firstRow, 1)
            }
        }
        manSourceTable.setShowColumns(true)

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
            .panel
    }

    fun getPanel(): JPanel? {
        return myMainPanel
    }

    fun getPreferredFocusedComponent(): JComponent {
        return manSetTable
    }

    private fun selectedManSet(): DataWrapper<ManSet>? {
        return if (manSetTable.selectedRow < 0) null else {
            manSetTableModel.getData(manSetTable.selectedRow)
        }
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
