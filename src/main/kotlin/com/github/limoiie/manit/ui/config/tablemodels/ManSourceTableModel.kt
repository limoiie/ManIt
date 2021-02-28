package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.database.dao.ManSource
import com.github.limoiie.manit.database.dsl.ManSetSources
import com.github.limoiie.manit.services.ManDbAppService.ManDbService
import com.intellij.openapi.application.ApplicationManager
import com.jetbrains.rd.util.Maybe
import io.reactivex.rxjava3.core.Observable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class ManSourceTableModel(
    boundManSet: Observable<Maybe<DataWrapper<ManSet>>>
) : DbTableModel<ManSource>() {

    companion object {
        val columnNames = listOf("", "Man Source Path", "Valid Files Count")
    }

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
                    PageState()
                } else {
                    val selected = transaction {
                        newManSet.rawData!!.sources.map(ManSource::id).toSet()
                    }
                    PageState(
                        data.filter { it.rawData?.id in selected }.toMutableSet()
                    )
                }
        }

        currManSet = newManSet

        fireTableDataChanged()
    }

    // override [TableModel]

    override fun getRowCount(): Int {
        return if (currManSet == null) 0 else {
            super.getRowCount()
        }
    }

    override fun getColumnCount(): Int = 3

    override fun getColumnName(column: Int): String = columnNames[column]

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

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex != 2 // the last column counting files should be read-only
    }

    // override [ItemRemovable]

    override fun removeRow(idx: Int) {
        pagesByManSet.values.forEach {
            it.selected.remove(data[idx])
        }
        super.removeRow(idx)
    }

    // override [DbTableModel]

    override fun fetchData(manDbService: ManDbService) = manDbService.allManSources

    override fun rowViewData(item: ManSource?): MutableList<Any?> {
        return mutableListOf(item?.path ?: "", item?.files?.count() ?: 0)
    }

    override fun upsert(data: DataWrapper<ManSource>) {
        val pathValue = data.viewData[0] as String
        val assign: ManSource.() -> Unit = {
            path = pathValue
        }
        if (data.isAdded()) {
            data.rawData = ManSource.new(assign)
        } else {
            data.rawData!!.apply(assign)
        }
    }

    override fun isModified(): Boolean {
        return super.isModified() || pagesByManSet.any { it.value.isModified() }
    }

    override fun applyToDb() {
        super.applyToDb()

        pagesByManSet.forEach { (manSet, st) ->
            (st.initSelected - st.selected).forEach { manSource ->
                ManSetSources.deleteWhere {
                    (ManSetSources.manSet eq manSet.rawData!!.id) and
                            (ManSetSources.manSource eq manSource.rawData!!.id)
                }
            }
            (st.selected - st.initSelected).forEach { manSource ->
                ManSetSources.insert {
                    it[ManSetSources.manSet] = manSet.rawData!!.id
                    it[ManSetSources.manSource] = manSource.rawData!!.id
                }
            }
        }

        pagesByManSet.clear()
    }

    override fun reset() {
        super.reset()

        pagesByManSet.clear()
        if (currManSet?.isAdded() == true) {
            // if the bounded manSet was new created, it shall become invalid now
            currManSet = null
        }
    }

    private class PageState(
        val selected: MutableSet<DataWrapper<ManSource>> = mutableSetOf()
    ) {
        val initSelected = selected.toSet()

        fun isModified() = initSelected.size != selected.size ||
                !initSelected.containsAll(selected)
    }

}
