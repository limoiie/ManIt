package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.services.ManDbAppService
import com.intellij.openapi.components.service
import com.intellij.util.ui.EditableModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.Entity
import javax.swing.table.AbstractTableModel

abstract class DbTableModel<T : Entity<*>> : AbstractTableModel(), EditableModel {
    private val db
        get() = service<ManDbAppService>()

    /**
     * The data that will be shown on or edited by view.
     *
     * When persisting is required, the data in [data] will be updated
     * into [db].
     */
    protected var data: MutableList<DataWrapper<T>> = mutableListOf()

    private val removedData: MutableList<DataWrapper<T>> = mutableListOf()

    protected open val ignoredIndexes: Set<Int> = setOf()

    protected abstract fun fetchData(manDbService: ManDbAppService.ManDbService): List<T>

    protected abstract fun rowViewData(item: T?): MutableList<Any?>

    protected fun loadData() {
        val rawDataList = runBlocking {
            val channel = Channel<List<T>>()
            db.addOnIndexedListener {
                val data = fetchData(this@addOnIndexedListener)
                GlobalScope.launch {
                    channel.send(data)
                }
            }
            channel.receive()
        }

        data = rawDataList
            .map { DataWrapper(it, rowViewData(it), ignoredIndexes) }
            .toMutableList()
    }

    fun getData(raw: Int): DataWrapper<T> = data[raw]

    open fun isModified(): Boolean {
        return removedData.isNotEmpty() || // deleted
                data.any {
                    it.rawData == null || // inserted
                            it.isModified() // updated
                }
    }

    // override [TableModel]'s member functions

    override fun getRowCount(): Int = data.size

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        data[rowIndex].viewData[columnIndex] = aValue
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return data[rowIndex].viewData[columnIndex]
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = true

    // override [ItemRemovable]

    override fun removeRow(idx: Int) {
        val row = data.removeAt(idx)
        if (row.rawData != null) {
            removedData.add(row)
        }
        fireTableRowsDeleted(idx, idx)
    }

    // override [EditableModel]'s member functions

    override fun addRow() {
        data.add(DataWrapper(null, rowViewData(null), ignoredIndexes))
        fireTableRowsInserted(data.lastIndex, data.lastIndex)
    }

    override fun exchangeRows(oldIndex: Int, newIndex: Int) {
        val old = data[oldIndex]
        data[oldIndex] = data[newIndex]
        data[newIndex] = old
        fireTableRowsUpdated(oldIndex, oldIndex)
        fireTableRowsUpdated(newIndex, newIndex)
    }

    override fun canExchangeRows(oldIndex: Int, newIndex: Int): Boolean = true

    class DataWrapper<T>(
        val rawData: T?,
        val viewData: MutableList<Any?>,
        private val ignoredIndexes: Set<Int>
    ) {
        private val initViewData = viewData.toList()

        fun isModified() = initViewData.size != viewData.size ||
            initViewData.zip(viewData)
            .filterIndexed { i, _ -> i !in ignoredIndexes }
            .any { (l, r) -> l != r }
    }
}
