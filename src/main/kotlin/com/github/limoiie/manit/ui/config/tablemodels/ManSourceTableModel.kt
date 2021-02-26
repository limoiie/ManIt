package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.database.dao.ManSource
import com.github.limoiie.manit.services.ManDbAppService.ManDbService
import org.jetbrains.exposed.sql.transactions.transaction

class ManSourceTableModel : DbTableModel<ManSource>() {

    private var currManSet: DataWrapper<ManSet>? = null

    private var manSets: MutableMap<DataWrapper<ManSet>, PageState> = mutableMapOf()

    init {
        loadData()
    }

    fun switchTo(newManSet: DataWrapper<ManSet>?) {
        if (newManSet == currManSet) return

        if (newManSet != null && newManSet !in manSets) {
            manSets[newManSet] =
                if (newManSet.rawData == null) {
                    PageState()
                } else {
                    val selected = transaction {
                        newManSet.rawData.sources.map(ManSource::id).toSet()
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

    override fun getColumnCount(): Int = 2

    // override [AbstractTableModel]

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex != 0) super.setValueAt(aValue, rowIndex, columnIndex - 1)
        else {
            manSets[currManSet]!!.selected.op(data[rowIndex], aValue as Boolean)
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return if (columnIndex != 0) super.getValueAt(rowIndex, columnIndex - 1)
        else {
            data[rowIndex] in manSets[currManSet]!!.selected
        }
    }

    // override [ItemRemovable]

    override fun removeRow(idx: Int) {
        manSets.values.forEach {
            it.selected.remove(data[idx])
        }
        super.removeRow(idx)
    }

    // override [DbTableModel]

    override fun fetchData(manDbService: ManDbService) = manDbService.allManSources

    override fun rowViewData(item: ManSource?): MutableList<Any?> {
        return mutableListOf(item?.path ?: "")
    }

    override fun isModified(): Boolean {
        return super.isModified() || manSets.any { it.value.isModified() }
    }

    private class PageState(
        val selected: MutableSet<DataWrapper<ManSource>> = mutableSetOf()
    ) {
        private val initSelected = selected.toSet()

        fun isModified() = initSelected.size != selected.size ||
                !initSelected.containsAll(selected)
    }

    private fun MutableSet<DataWrapper<ManSource>>.op(
        element: DataWrapper<ManSource>,
        addOrRemove: Boolean
    ) {
        if (addOrRemove) add(element) else remove(element)
    }
}
