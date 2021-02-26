package com.github.limoiie.manit.ui.config

import com.github.limoiie.manit.ui.config.tablemodels.ManSetTableModel
import com.github.limoiie.manit.ui.config.tablemodels.ManSourceTableModel
import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null
    private var manSetTableModel: ManSetTableModel? = null
    private var manSourceTableModel: ManSourceTableModel? = null

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Man It"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return mySettingsComponent!!.getPreferredFocusedComponent()
    }

    override fun createComponent(): JComponent? {
        manSetTableModel = ManSetTableModel()
        manSourceTableModel = ManSourceTableModel()
        mySettingsComponent = AppSettingsComponent(
            manSetTableModel!!, manSourceTableModel!!
        )
        return mySettingsComponent!!.getPanel()
    }

    override fun isModified(): Boolean {
        return manSetTableModel!!.isModified() || manSourceTableModel!!.isModified()
    }

    override fun apply() {
        val settings: AppSettingsState = AppSettingsState.instance()
        settings.userId = mySettingsComponent!!.getUserNameText()
        settings.ideaStatus = mySettingsComponent!!.getIdeaUserStatus()
    }

    override fun reset() {
        val settings: AppSettingsState = AppSettingsState.instance()
        mySettingsComponent!!.setUserNameText(settings.userId)
        mySettingsComponent!!.setIdeaUserStatus(settings.ideaStatus)
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}