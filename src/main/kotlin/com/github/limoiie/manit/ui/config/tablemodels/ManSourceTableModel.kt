package com.github.limoiie.manit.ui.config.tablemodels

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.database.dao.ManSource
import com.github.limoiie.manit.database.dsl.ManSetSources
import com.github.limoiie.manit.services.ManDbAppService.ManDbService
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class ManSourceTableModel : DbTableModel<ManSource>() {

    private var currManSet: DataWrapper<ManSet>? = null

    private var manSets: MutableMap<DataWrapper<ManSet>, PageState> = mutableMapOf()

    fun switchTo(newManSet: DataWrapper<ManSet>?) {
        if (newManSet == currManSet) return

        if (newManSet != null && newManSet !in manSets) {
            manSets[newManSet] =
                if (newManSet.rawData == null) {
                    PageState()
                } else {
                    val selected = transaction {
                        newManSet.rawData!!.sources.map(ManSource::id).toSet()
                    }
                    PageState(
                        data.filter { it.rawData?.id in selected }.toMutableSet()
                    )
                }
        }

        currManSet = newManSet

        fireTableDataChanged()
    }

    // override [TableModel]

    override fun getRowCount(): Int {
        return if (currManSet == null) 0 else {
            super.getRowCount()
        }
    }

    override fun getColumnCount(): Int = 3

    // override [AbstractTableModel]

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex != 0) super.setValueAt(aValue, rowIndex, columnIndex - 1)
        else {
            val selected = manSets[currManSet]!!.selected
            if (aValue as Boolean) selected.add(data[rowIndex])
            else {
                selected.remove(data[rowIndex])
            }
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        return if (columnIndex != 0) super.getValueAt(rowIndex, columnIndex - 1)
        else {
            data[rowIndex] in manSets[currManSet]!!.selected
        }
    }

    // override [ItemRemovable]

    override fun removeRow(idx: Int) {
        manSets.values.forEach {
            it.selected.remove(data[idx])
        }
        super.removeRow(idx)
    }

    // override [DbTableModel]

    override fun fetchData(manDbService: ManDbService) = manDbService.allManSources

    override fun rowViewData(item: ManSource?): MutableList<Any?> {
        return mutableListOf(item?.path ?: "", item?.files?.count() ?: 0)
    }

    override fun upsert(data: DataWrapper<ManSource>) {
        val pathValue = data.viewData[0] as String
        val assign: ManSource.() -> Unit = {
            path = pathValue
        }
        if (data.isAdded()) {
            data.rawData = ManSource.new(assign)
        } else {
            data.rawData!!.apply(assign)
        }
    }

    override fun isModified(): Boolean {
        return super.isModified() || manSets.any { it.value.isModified() }
    }

    override fun applyToDb() {
        super.applyToDb()

        manSets.forEach { (manSet, st) ->
            (st.initSelected - st.selected).forEach { manSource ->
                ManSetSources.deleteWhere {
                    (ManSetSources.manSet eq manSet.rawData!!.id) and
                            (ManSetSources.manSource eq manSource.rawData!!.id)
                }
            }
            (st.selected - st.initSelected).forEach { manSource ->
                ManSetSources.insert {
                    it[ManSetSources.manSet] = manSet.rawData!!.id
                    it[ManSetSources.manSource] = manSource.rawData!!.id
                }
            }
        }

        manSets.clear()
    }

    private class PageState(
        val selected: MutableSet<DataWrapper<ManSource>> = mutableSetOf()
    ) {
        val initSelected = selected.toSet()

        fun isModified() = initSelected.size != selected.size ||
                !initSelected.containsAll(selected)
    }

}
