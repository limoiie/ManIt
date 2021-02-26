package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.database.dao.ManSource
import com.github.limoiie.manit.services.ManDbAppService
import com.github.limoiie.manit.services.impls.ManIndex
import org.jetbrains.exposed.sql.transactions.transaction

class ManSourceTableModel : DbTableModel<ManSource>() {

    private var manSet: ManSet? = null

    private var selectedSources: List<ManSource> = listOf()

    init {
        loadData()
    }

    override fun getColumnCount(): Int = 2

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return manSet?.name != ManIndex.nameOfAllManSet
    }

    override fun fetchData(manDbService: ManDbAppService.ManDbService): List<ManSource> {
        return manDbService.allManSources
    }

    override fun rowViewData(item: ManSource): MutableList<Any?> {
        return mutableListOf(
            item.id in selectedSources.map(ManSource::id),
            item.path
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

    fun bindManSet(newManSet: ManSet) {
        manSet = newManSet
        transaction {
            selectedSources = newManSet.sources.toList()
        }
        refreshViewData()
        fireTableDataChanged()
    }
}
