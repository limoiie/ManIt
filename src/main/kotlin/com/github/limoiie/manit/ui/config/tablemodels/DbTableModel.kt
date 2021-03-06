package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.services.ManDbAppService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.ui.EditableModel
import org.jetbrains.exposed.dao.Entity
import javax.swing.table.AbstractTableModel

abstract class DbTableModel<T : Entity<*>> : AbstractTableModel(), EditableModel {
    private val db
        get() = service<ManDbAppService>()

    private val logger = logger<DbTableModel<T>>()

    /**
     * The data that will be shown on or edited by view.
     *
     * When persisting is required, the data in [data] will be updated
     * into [db].
     */
    protected var data: MutableList<DataWrapper<T>> = mutableListOf()

    private val removedData: MutableList<DataWrapper<T>> = mutableListOf()

    protected open val ignoredIndexes: Set<Int> = setOf()

    init {
        db.service.subscribe { result ->
            result.asNullable?.apply {
                reloadData(this)
            }
        }
    }

    protected abstract fun fetchData(manDbService: ManDbAppService.ManDbService): List<T>

    protected abstract fun rowViewData(item: T?): MutableList<Any?>

    protected abstract fun rowDataView(view: MutableList<Any?>): T.() -> Unit

    protected abstract fun new(init: T.() -> Unit): T

    private fun upsert(data: DataWrapper<T>) {
        if (data.isAdded()) {
            data.rawData = new(rowDataView(data.viewData))
        } else {
            data.rawData!!.apply(rowDataView(data.viewData))
        }
    }

    fun getData(raw: Int): DataWrapper<T> = data[raw]

    open fun isModified(): Boolean {
        return removedData.isNotEmpty() || // deleted
                data.any {
                    it.isAdded() || it.isUpdated()
                }
    }

    protected open fun reloadData(manDbService: ManDbAppService.ManDbService) {
        val newData = manDbService.doFind {
            fetchData(manDbService)
                .map { DataWrapper(it, rowViewData(it), ignoredIndexes) }
                .toMutableList()
        }

        data = newData
        removedData.clear()

        // bug: here is a really strange bug that following action won't be executed
        // until exiting the preference page, under the case that opening preference page
        // directly
        ApplicationManager.getApplication().invokeLater {
            fireTableDataChanged()
        }
    }

    open fun applyToDb() {
        removedData.forEach {
            it.rawData!!.delete()
        }

        data.forEach {
            if (it.isAdded() || it.isUpdated()) {
                upsert(it)
            }
        }
    }

    open fun reset() {
        data = data.union(removedData)
            .filter { !it.isAdded() }
            .onEach {
                it.reset()
            }.toMutableList()

        removedData.clear()
        fireTableDataChanged()
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

    // todo - try to support record the order
    class DataWrapper<T>(
        var rawData: T?,
        val viewData: MutableList<Any?>,
        private val ignoredIndexes: Set<Int>
    ) {
        private val initViewData = viewData.toList()

        fun isAdded() = rawData == null

        fun isUpdated() = initViewData.size != viewData.size ||
                initViewData.zip(viewData)
                    .filterIndexed { i, _ -> i !in ignoredIndexes }
                    .any { (l, r) -> l != r }

        fun reset() {
            viewData.clear()
            viewData.addAll(initViewData)
        }
    }
}
