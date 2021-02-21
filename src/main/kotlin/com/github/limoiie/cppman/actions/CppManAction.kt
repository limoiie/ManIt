package com.github.limoiie.cppman.actions

import com.github.limoiie.cppman.OCUtils
import com.github.limoiie.cppman.isInlineNamespace
import com.github.limoiie.cppman.services.MyApplicationService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.psi.PsiElement
import com.jetbrains.cidr.lang.psi.OCSymbolDeclarator
import com.jetbrains.cidr.lang.symbols.OCSymbol
import com.jetbrains.cidr.lang.symbols.OCSymbolWithParent


class CppManAction : AnAction() {
    private val logger = logger<CppManAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val psiElem = e.getData(CommonDataKeys.PSI_ELEMENT)
        if (psiElem != null) {
            logger.debug { "PSI under cursor: ${OCUtils.debugElem(psiElem)}" }
            val qualifiedName = getQualifiedName(psiElem)
            if (qualifiedName != null) {
                logger.info("Qualified Name: $qualifiedName")
                service<MyApplicationService>().man(qualifiedName, MyApplicationService.ManType.CppMan)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null &&
                e.getData(CommonDataKeys.PSI_ELEMENT) != null
    }

    /**
     * Get the full-qualified name for [element] in type PsiElement.
     *
     * For example, given element 'OCDeclarator(seconds)', return "std::chrono::seconds"
     */
    private fun getQualifiedName(element: PsiElement): String? {
        if (element is OCSymbolDeclarator<*> && element.isValid) {
            val symbols = mutableListOf<OCSymbol>()
            var sym = element.symbol
            while (sym is OCSymbolWithParent) {
                logger.debug { "Symbol($sym) -> ${(sym as OCSymbolWithParent).type}" }

                if (filterSymbols(sym)) {
                    symbols.add(sym)
                }
                sym = sym.parent
            }
            return symbols.asReversed()
                .joinToString(separator = "::") { it.name }
        }
        return null
    }

    /**
     * Filter out those [sym] that may not present in the absolute qualified
     * name, such as the inline namespace.
     */
    private fun filterSymbols(sym: OCSymbol): Boolean {
        // inline namespace is skipped
        return !sym.isInlineNamespace()
    }

}