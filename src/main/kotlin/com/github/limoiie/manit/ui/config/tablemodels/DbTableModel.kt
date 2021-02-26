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

    protected var rawData: List<T> = listOf()

    /**
     * The data that will be shown on or edited by view.
     *
     * When persisting is required, the data in [viewData] will be updated
     * into [db]. **Note**: the first column of [viewData] will be filled
     * with data's id automatically. Although the id will not be shown on
     * the table, it will be used to update [db] when required.
     */
    private var viewData: MutableList<MutableList<Any?>> = mutableListOf()

    override fun getRowCount(): Int {
        return viewData.size
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        viewData[rowIndex][columnIndex + 1] = aValue
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return viewData[rowIndex][columnIndex + 1]
    }

    protected fun loadData() {
        rawData = runBlocking {
            val channel = Channel<List<T>>()
            db.addOnIndexedListener {
                val data = fetchData(this@addOnIndexedListener)
                GlobalScope.launch {
                    channel.send(data)
                }
            }
            channel.receive()
        }
        refreshViewData()
    }

    protected fun refreshViewData() {
        viewData = rawData.map { rawData ->
            rowViewData(rawData).apply {
                add(0, rawData.id)
            }
        }.toMutableList()
    }

    fun getRawData(raw: Int): T {
        return rawData[raw]
    }

    protected abstract fun fetchData(manDbService: ManDbAppService.ManDbService): List<T>

    protected abstract fun rowViewData(item: T): MutableList<Any?>
}
