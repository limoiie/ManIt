package com.github.limoiie.manit.ui.components

import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList

class ComboBoxTooltipRender(private val tooltips: List<String>) : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val comp = super.getListCellRendererComponent(
            list, value, index, isSelected, cellHasFocus
        ) as JComponent

        if (-1 < index && null != value) {
            comp.toolTipText = tooltips[index]
        }
        return comp
    }
}
