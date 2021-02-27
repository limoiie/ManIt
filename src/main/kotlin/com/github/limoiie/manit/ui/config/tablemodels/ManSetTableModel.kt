package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.services.ManDbAppService.ManDbService

class ManSetTableModel : DbTableModel<ManSet>() {
    private var columnNames = listOf("Man Sets")

    override fun getColumnCount(): Int = 1

    override fun getColumnName(column: Int): String = columnNames[column]

    override fun fetchData(manDbService: ManDbService) = manDbService.allManSets

    override fun rowViewData(item: ManSet?): MutableList<Any?> {
        return mutableListOf(
            item?.name ?: "Unknown"
        )
    }

    override fun upsert(data: DataWrapper<ManSet>) {
        val nameValue = data.viewData[0] as String
        val assign: ManSet.() -> Unit = {
            name = nameValue
        }
        if (data.isAdded()) {
            data.rawData = ManSet.new(assign)
        } else {
            data.rawData!!.apply(assign)
        }
    }
}
