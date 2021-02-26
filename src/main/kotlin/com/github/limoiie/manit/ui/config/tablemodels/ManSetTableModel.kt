package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.services.ManDbAppService

class ManSetTableModel : DbTableModel<ManSet>() {
    init {
        loadData()
    }

    override fun getColumnCount(): Int = 1

    override fun getColumnName(column: Int): String {
        return "Man Sets"
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return rawData[rowIndex].name
    }

    override fun fetchData(manDbService: ManDbAppService.ManDbService): List<ManSet> {
        return manDbService.allManSets
    }

    override fun rowViewData(item: ManSet): MutableList<Any?> {
        return mutableListOf(
            item.name
        )
    }

    override fun removeRow(idx: Int) {
        TODO("Not yet implemented")
    }

    override fun addRow() {
        TODO("Not yet implemented")
    }

    override fun exchangeRows(oldIndex: Int, newIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun canExchangeRows(oldIndex: Int, newIndex: Int): Boolean {
        return false
    }
}
