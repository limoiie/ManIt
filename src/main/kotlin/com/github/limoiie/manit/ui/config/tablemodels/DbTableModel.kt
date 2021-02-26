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
            .map { DataWrapper(it, rowViewData(it)) }
            .toMutableList()
    }

    fun getData(raw: Int): DataWrapper<T> {
        return data[raw]
    }

    override fun getRowCount(): Int {
        return data.size
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        data[rowIndex].viewData[columnIndex] = aValue
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return data[rowIndex].viewData[columnIndex]
    }

    override fun addRow() {
        data.add(DataWrapper(null, rowViewData(null)))
    }

    override fun removeRow(idx: Int) {
        removedData.add(data.removeAt(idx))
    }

    override fun exchangeRows(oldIndex: Int, newIndex: Int) {
        val old = data[oldIndex]
        data[oldIndex] = data[newIndex]
        data[newIndex] = old
    }

    override fun canExchangeRows(oldIndex: Int, newIndex: Int): Boolean {
        return true
    }

    protected abstract fun fetchData(manDbService: ManDbAppService.ManDbService): List<T>

    protected abstract fun rowViewData(item: T?): MutableList<Any?>

    class DataWrapper<T>(val rawData: T?, val viewData: MutableList<Any?>)
}
