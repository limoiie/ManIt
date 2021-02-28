package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.database.dao.ManSource
import com.github.limoiie.manit.services.ManDbAppService.ManDbService

open class ManSourceTableModel : DbTableModel<ManSource>() {

    open val columnNames = listOf("Man Source Path", "Valid Files Count")

    override fun getColumnCount(): Int = 2

    override fun getColumnName(column: Int): String = columnNames[column]

    // override [AbstractTableModel]

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        // the last column who counting files should be read-only
        return columnIndex != columnCount - 1
    }

    // override [DbTableModel]

    override fun fetchData(manDbService: ManDbService) = manDbService.allManSources

    override fun rowViewData(item: ManSource?): MutableList<Any?> {
        return mutableListOf(item?.path ?: "", item?.files?.count() ?: 0)
    }

    override fun rowDataView(view: MutableList<Any?>): ManSource.() -> Unit = {
        path = view[0] as String
    }

    override fun new(init: ManSource.() -> Unit) = ManSource.new(init)

}
