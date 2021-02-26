package com.github.limoiie.manit.ui.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "ManIt.AppSettingsState",
    storages = [Storage("ManItSettings.xml")]
)
class AppSettingsState : PersistentStateComponent<AppSettingsState> {
    companion object {
        fun instance() = service<AppSettingsState>()
    }

    var userId = "John Q. Public"
    var ideaStatus = false

    override fun getState(): AppSettingsState {
        return this
    }

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
