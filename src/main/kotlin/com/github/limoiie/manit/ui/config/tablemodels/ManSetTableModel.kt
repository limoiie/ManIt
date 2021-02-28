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

    override fun rowDataView(view: MutableList<Any?>): ManSet.() -> Unit = {
        name = view[0] as String
    }

    override fun new(init: ManSet.() -> Unit) = ManSet.new(init)

}
