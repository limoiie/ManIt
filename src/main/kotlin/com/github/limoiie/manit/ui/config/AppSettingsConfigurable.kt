package com.github.limoiie.manit.ui.config

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

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
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent!!.getPanel()
    }

    override fun isModified(): Boolean {
        val settings: AppSettingsState = AppSettingsState.instance()
        var modified = mySettingsComponent!!.getUserNameText() != settings.userId
        modified =
            modified or (mySettingsComponent!!.getIdeaUserStatus() != settings.ideaStatus)
        return modified
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
