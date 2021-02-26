package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.database.dao.ManSource
import com.github.limoiie.manit.services.ManDbAppService
import com.github.limoiie.manit.services.impls.ManIndex
import org.jetbrains.exposed.sql.transactions.transaction

class ManSourceTableModel : DbTableModel<ManSource>() {

    private var manSet: DataWrapper<ManSet>? = null

    private var manSets: MutableMap<DataWrapper<ManSet>, PageState> = mutableMapOf()

    private var selectedSources: List<ManSource> = listOf()

    init {
        loadData()
    }

    override fun getColumnCount(): Int = 2

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return manSet?.rawData?.name != ManIndex.nameOfAllManSet
    }

    override fun fetchData(manDbService: ManDbAppService.ManDbService): List<ManSource> {
        return manDbService.allManSources
    }

    override fun rowViewData(item: ManSource?): MutableList<Any?> {
        return if (item != null) {
            mutableListOf(
                item.id in selectedSources.map(ManSource::id),
                item.path
            )
        } else {
            mutableListOf(false, "")
        }
    }

    fun bindManSet(newManSet: DataWrapper<ManSet>) {
        if (newManSet == manSet) return

        if (newManSet !in manSets) {
            manSets[newManSet] =
                if (newManSet.rawData == null) {
                    PageState(List(data.size) { false })
                } else {
                    val selectedSources = transaction {
                        newManSet.rawData.sources.map(ManSource::id)
                    }
                    PageState(data.map { it.rawData?.id in selectedSources })
                }
        }

        savePageStateFromView()
        manSet = newManSet
        loadPageStateToView()

        fireTableDataChanged()
    }

    private fun savePageStateFromView() {
        if (manSet != null) {
            manSets[manSet!!] = PageState(data.map { it.viewData[0] as Boolean })
        }
    }

    private fun loadPageStateToView() {
        if (manSet != null) {
            val st = manSets[manSet!!]
            if (st != null) {
                data.forEachIndexed { i, it ->
                    it.viewData[0] = st.selected[i]
                }
            }
        }
    }

    private class PageState(val selected: List<Boolean>)
}
