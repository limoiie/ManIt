package com.github.limoiie.manit.toolwindows

import com.github.limoiie.manit.database.dao.ManSection
import com.github.limoiie.manit.database.dao.ManSet
import com.github.limoiie.manit.services.ManDbAppService
import com.github.limoiie.manit.ui.components.ComboBoxTooltipRender
import com.intellij.codeInsight.documentation.DocumentationComponent
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.structuralsearch.plugin.ui.TextFieldWithAutoCompletionWithBrowseButton
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.JPanel

class CppManToolWindow(private val project: Project) {
    private val logger = logger<CppManToolWindow>()

    // Ui fields:

    private val toolWindowContent = JPanel()

    private val topPanel = JPanel()
    private val valueTxt = TextFieldWithAutoCompletionWithBrowseButton(project)

    private val configPanel = JPanel()
    private val manSetBox = ComboBox<ManSet>()
    private val manSectionBox = ComboBox<ManSection>()

    private var manpagePanel: DocumentationComponent? = null

    // State fields:

    private val manSet get() = manSetBox.selectedItem as ManSet
    private val manSection get() = manSectionBox.selectedItem as ManSection

    companion object {
        const val maxExeBoxWidth = 96
        const val maxSectionBoxWidth = 48
    }

    init {
        service<ManDbAppService>().addOnIndexedListener {
            ApplicationManager.getApplication().invokeLater {
                allManSets.forEach { manSetBox.addItem(it) }
                allManSections.forEach { manSectionBox.addItem(it) }

                val tooltips = makeManSectionTooltip(allManSections)
                manSectionBox.renderer = ComboBoxTooltipRender(tooltips)
                initUi()
            }
        }
    }

    fun getContent(): JPanel = toolWindowContent

    private fun initUi() {
        logger.debug { "initUi" }

        // man config panel

        val actionUpdateConfigPanel = { it: ItemEvent ->
            logger.debug { "Man changed: $it" }
            if (it.stateChange == ItemEvent.SELECTED) {
                loadManCandidates()
            }
        }

        manSetBox.selectedItem = null
        manSetBox.addItemListener(actionUpdateConfigPanel)
        manSetBox.selectedIndex = 0 // set default man
        manSetBox.toolTipText = "Man executable"

        manSectionBox.addItemListener(actionUpdateConfigPanel)
        manSectionBox.selectedIndex = 0 // set default man
        manSectionBox.toolTipText = "Man section"

        manSetBox.setMinimumAndPreferredWidth(maxExeBoxWidth)
        manSectionBox.setMinimumAndPreferredWidth(maxSectionBoxWidth)

        configPanel.layout = FlowLayout(FlowLayout.LEFT, 0, 0)
        configPanel.add(manSetBox)
        configPanel.add(manSectionBox)

        // man word panel

        valueTxt.childComponent.toolTipText = "The value to man with"
        valueTxt.childComponent.setPlaceholder("std::*")
        valueTxt.setButtonIcon(AllIcons.Actions.Refresh)
        valueTxt.addActionListener {
            // todo - load manpage
            service<ManDbAppService>().addOnIndexedListener {
                val keyword = valueTxt.text
                manpage(keyword, manSet, selectedSections()) {
                    ApplicationManager.getApplication().invokeLater {
                        showManPage(keyword, it)
                    }
                }
            }
        }

        // top panel = man config panel + man word panel

        topPanel.layout = BorderLayout()
        topPanel.add(valueTxt, BorderLayout.CENTER)
        topPanel.add(configPanel, BorderLayout.WEST)

        // man page panel

        manpagePanel = createDocumentationComponent()

        // tool window content = top panel + man page panel

        toolWindowContent.layout = BorderLayout()
        toolWindowContent.add(topPanel, BorderLayout.NORTH)
        toolWindowContent.add(manpagePanel!!, BorderLayout.CENTER)
    }

    private fun loadManCandidates() {
        logger.debug { "load candidates for $manSet" }

        service<ManDbAppService>().addOnIndexedListener {
            keywords(manSet, selectedSections()) {
                valueTxt.setAutoCompletionItems(it)
            }
        }
    }

    fun showManPage(word: String, manPage: String? = null) {
        logger.debug { "updateUi with man value $word" }

        valueTxt.text = word

        val doc = manPage ?: "<i>The Man Page Does Not Exist!!</i>"
        manpagePanel?.apply {
            setData(element, doc, null, null, null)
        }
    }

    private fun createDocumentationComponent(doc: String = ""): DocumentationComponent {
        val docManager = DocumentationManager.getInstance(project)
        val storeSize = false
        return DocumentationComponent(docManager, storeSize).apply {
            setData(element, doc, null, null, null)
        }
    }

    private fun selectedSections(): List<ManSection> {
        return listOf(manSection) // todo - check and expand all-section
    }
}
