package com.github.limoiie.manit.ui.config

import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.services.ManDbAppService
import com.github.limoiie.manit.ui.config.tablemodels.DbTableModel.DataWrapper
import com.github.limoiie.manit.ui.config.tablemodels.ManSetTableModel
import com.github.limoiie.manit.ui.config.tablemodels.ManSourcePageTableModel
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.jetbrains.rd.util.Maybe
import io.reactivex.rxjava3.subjects.PublishSubject
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null
    private var manSetTableModel: ManSetTableModel? = null
    private var manSourceTableModel: ManSourcePageTableModel? = null

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
        val selectedManSet = PublishSubject.create<Maybe<DataWrapper<ManSet>>>()

        manSetTableModel = ManSetTableModel()
        manSourceTableModel = ManSourcePageTableModel(selectedManSet)
        mySettingsComponent = AppSettingsComponent(
            manSetTableModel!!, manSourceTableModel!!, selectedManSet
        )
        return mySettingsComponent!!.getPanel()
    }

    override fun isModified(): Boolean {
        return manSetTableModel!!.isModified() || manSourceTableModel!!.isModified()
    }

    override fun apply() {
        service<ManDbAppService>().untilReady {
            doUpdate {
                manSetTableModel!!.applyToDb()
                manSourceTableModel!!.applyToDb()
            }
        }

        service<ManDbAppService>().indexManRepo()
    }

    override fun reset() {
        manSetTableModel?.reset()
        manSourceTableModel?.reset()
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
