package com.github.lppedd.cc.lookupElement

import com.github.lppedd.cc.*
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.ValidToken
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.psi.PsiElement

/**
 * @author Edoardo Luppi
 */
internal class CommitScopeLookupElement(
    override val index: Int,
    private val psiElement: CommitScopePsiElement,
) : CommitLookupElement() {
  override val weight: UInt = WEIGHT_SCOPE

  override fun getPsiElement(): PsiElement =
    psiElement

  override fun getLookupString(): String =
    psiElement.commitScope.text

  override fun renderElement(presentation: LookupElementPresentation) {
    val commitScope = psiElement.commitScope
    val rendering = commitScope.getRendering()
    presentation.itemText = commitScope.text
    presentation.icon = ICON_SCOPE
    presentation.isItemTextBold = rendering.bold
    presentation.isItemTextItalic = rendering.italic
    presentation.isStrikeout = rendering.strikeout
    presentation.isTypeIconRightAligned = true
    presentation.setTypeText(rendering.type, rendering.icon)
  }

  override fun handleInsert(context: InsertionContext) {
    val editor = context.editor
    val document = context.document

    val (lineStart, lineEnd) = editor.getCurrentLineRange()
    val lineText = document.getSegment(lineStart until lineEnd)
    val (type, _, breakingChange, _, subject) = CCParser.parseHeader(lineText)
    val text = StringBuilder(150)

    // If a type had been specified, we need to insert it again
    // starting from the original position
    val typeStartOffset = if (type is ValidToken) {
      text += type.value
      lineStart + type.range.first
    } else {
      lineStart
    }

    // We insert the new scope
    text += "($lookupString)"

    // If a breaking change indicator was present, we insert it back
    text += if (breakingChange.isPresent) "!:" else ":"

    // If a subject had been specified, we insert it back
    val subjectValue = (subject as? ValidToken)?.value.orWhitespace()
    val textLengthWithoutSubject = text.length + if (subjectValue.firstIsWhitespace()) 1 else 0

    document.replaceString(typeStartOffset, lineEnd, text + subjectValue)
    editor.moveCaretToOffset(typeStartOffset + textLengthWithoutSubject)
    editor.scheduleAutoPopup()
  }
}
