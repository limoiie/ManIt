package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.database.dao.ManSource
import com.github.limoiie.manit.database.dsl.ManSetSources
import com.intellij.openapi.application.ApplicationManager
import com.jetbrains.rd.util.Maybe
import io.reactivex.rxjava3.core.Observable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class ManSourcePageTableModel(
    boundManSet: Observable<Maybe<DataWrapper<ManSet>>>
) : ManSourceTableModel() {

    override val columnNames = listOf("") + super.columnNames

    private var currManSet: DataWrapper<ManSet>? = null

    private var pagesByManSet: MutableMap<DataWrapper<ManSet>, PageState> = mutableMapOf()

    init {
        boundManSet.subscribe {
            ApplicationManager.getApplication().invokeLater {
                switchTo(it.asNullable)
            }
        }
    }

    private fun switchTo(newManSet: DataWrapper<ManSet>?) {
        if (newManSet != null && newManSet !in pagesByManSet) {
            pagesByManSet[newManSet] =
                if (newManSet.rawData == null) {
                    PageState(newManSet)
                } else {
                    val selected = transaction {
                        newManSet.rawData!!.sources.map(ManSource::id).toSet()
                    }
                    PageState(
                        newManSet,
                        data.filter { it.rawData?.id in selected }.toMutableSet()
                    )
                }
        }

        currManSet = newManSet

        fireTableDataChanged()
    }

    // override [TableModel]

    override fun getRowCount(): Int {
        return if (currManSet == null) 0 else super.getRowCount()
    }

    // One more column in index 0 for showing the selection
    override fun getColumnCount(): Int = 1 + super.getColumnCount()

    // override [AbstractTableModel]

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex != 0) super.setValueAt(aValue, rowIndex, columnIndex - 1)
        else {
            val selected = pagesByManSet[currManSet]!!.selected
            if (aValue as Boolean) selected.add(data[rowIndex])
            else {
                selected.remove(data[rowIndex])
            }
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return if (columnIndex != 0) super.getValueAt(rowIndex, columnIndex - 1)
        else {
            data[rowIndex] in pagesByManSet[currManSet]!!.selected
        }
    }

    // override [ItemRemovable]

    override fun removeRow(idx: Int) {
        pagesByManSet.values.forEach {
            it.selected.remove(data[idx])
        }
        super.removeRow(idx)
    }

    // override [DbTableModel]

    override fun isModified(): Boolean {
        return super.isModified() || pagesByManSet.any { it.value.isModified() }
    }

    override fun applyToDb() {
        super.applyToDb()

        pagesByManSet.values.forEach(PageState::apply)
        pagesByManSet.clear()
    }

    override fun reset() {
        pagesByManSet.clear()
        currManSet = null
        super.reset()
    }

    private class PageState(
        val manSet: DataWrapper<ManSet>,
        val selected: MutableSet<DataWrapper<ManSource>> = mutableSetOf()
    ) {
        val initSelected = selected.toSet()

        fun isModified() = initSelected.size != selected.size ||
                !initSelected.containsAll(selected)

        fun apply() {
            val manSet = manSet
            // do the remove action
            (initSelected - selected).forEach { manSource ->
                ManSetSources.deleteWhere {
                    (ManSetSources.manSet eq manSet.rawData!!.id) and
                            (ManSetSources.manSource eq manSource.rawData!!.id)
                }
            }
            // do the insert action
            (selected - initSelected).forEach { manSource ->
                ManSetSources.insert {
                    it[ManSetSources.manSet] = manSet.rawData!!.id
                    it[ManSetSources.manSource] = manSource.rawData!!.id
                }
            }
        }
    }

}
