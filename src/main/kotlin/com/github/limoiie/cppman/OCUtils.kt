package com.github.limoiie.cppman

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.jetbrains.cidr.lang.psi.OCCppNamespace
import com.jetbrains.cidr.lang.symbols.OCSymbol
import com.jetbrains.cidr.lang.symbols.cpp.OCNamespaceSymbol

fun PsiElement.isInlineNamespace(): Boolean {
    if (this is OCCppNamespace) {
        val firstChild: PsiElement = this.firstChild ?: return false
        if (firstChild is PsiNamedElement) {
            return firstChild.name == "inline"
        }
    }
    return false
}

fun OCSymbol.isInlineNamespace(): Boolean {
    return this is OCNamespaceSymbol && this.isInlineNamespace
}

class OCUtils {
    companion object {
        /**
         * Print [psiElem] in type `PsiElement` recursively from bottom to root.
         *
         * This is used for debug only
         */
        fun debugElem(psiElem: PsiElement): String {
            var elem = psiElem
            val notificationContent = StringBuffer(" $elem\n")
            while (elem.parent != null) {
                elem = elem.parent
                notificationContent.append("\t-> of $elem\n")
            }
            return notificationContent.toString()
        }
    }
}
