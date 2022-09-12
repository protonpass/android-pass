package me.proton.core.pass.autofill.service

import android.app.assist.AssistStructure
import android.os.Build
import android.text.InputType
import android.view.View
import me.proton.core.pass.autofill.service.entities.AndroidAutofillFieldId
import me.proton.core.pass.autofill.service.entities.AssistField
import me.proton.core.pass.autofill.service.entities.AutofillNode
import me.proton.core.pass.common_secret.SecretType

class AssistNodeTraversal {

    companion object {
        const val HINT_CURRENT_PASSWORD = "current-password"
    }

    private var autoFillNodes = mutableListOf<AssistField>()

    // For testing purposes
    var visitedNodes = 0
        private set

    fun traverse(node: AssistStructure.ViewNode): List<AssistField> =
        traverse(node.toAutofillNode())

    fun traverse(node: AutofillNode): List<AssistField> {
        visitedNodes = 0
        autoFillNodes = mutableListOf()
        traverseInternal(node)
        return autoFillNodes
    }

    private fun traverseInternal(node: AutofillNode) {
        if (nodeSupportsAutoFill(node)) {
            val assistField = AssistField(
                node.id!!,
                detectFieldType(node),
                node.autofillValue,
                node.text.toString()
            )
            autoFillNodes.add(assistField)
        }
        node.children.forEach { traverseInternal(it) }

        visitedNodes += 1
    }

    private fun nodeSupportsAutoFill(node: AutofillNode): Boolean {
        val isImportant = node.isImportantForAutofill
        val hasAutoFillHints = node.autofillHints.isNotEmpty()
        val hasHtmlInfo = node.htmlAttributes.isNotEmpty()
        val hasValidInputType = nodeHasValidInputType(node)
        val hasAutofillInfo = hasValidInputType || hasAutoFillHints || hasHtmlInfo

        return node.id != null && hasAutofillInfo && isImportant
    }

    private fun nodeHasValidInputType(node: AutofillNode): Boolean {
        val inputType = node.inputType
        val hasMultilineFlag = inputType hasFlag InputType.TYPE_TEXT_FLAG_MULTI_LINE ||
            inputType hasFlag InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
        val hasAutoCorrectFlag = inputType hasFlag InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        // InputType.TYPE_TEXT_FLAG_CAP_SENTENCES might also be considered in the future
        return inputType != 0 && !hasMultilineFlag && !hasAutoCorrectFlag
    }

    private fun detectFieldType(node: AutofillNode): SecretType? {
        val autofillHint = node.autofillHints.firstOrNull()
        val htmlAttributes = node.htmlAttributes
        return when {
            htmlAttributes.isNotEmpty() -> detectFieldTypeUsingHtmlInfo(htmlAttributes)
            autofillHint != null -> detectFieldTypeUsingAutofillHint(autofillHint)
            else -> detectFieldTypeUsingInputType(node.inputType)
        }
    }

    fun detectFieldTypeUsingHtmlInfo(
        htmlAttributes: List<Pair<String, String>>
    ): SecretType? {
        val typeAttribute = htmlAttributes.firstOrNull { it.first == "type" }
        return when (typeAttribute?.second) {
            "tel" -> SecretType.Phone
            "email" -> SecretType.Email
            "password" -> SecretType.Password
            "text" -> SecretType.Other
            else -> null
        }
    }

    fun detectFieldTypeUsingAutofillHint(autofillHint: String) = when (autofillHint) {
        View.AUTOFILL_HINT_EMAIL_ADDRESS -> SecretType.Email
        View.AUTOFILL_HINT_NAME -> SecretType.FullName
        View.AUTOFILL_HINT_USERNAME -> SecretType.Username
        View.AUTOFILL_HINT_PASSWORD, HINT_CURRENT_PASSWORD -> SecretType.Password
        View.AUTOFILL_HINT_PHONE -> SecretType.Phone
        else -> null
    }

    fun detectFieldTypeUsingInputType(inputType: Int): SecretType? = when {
        inputType == InputType.TYPE_CLASS_PHONE -> SecretType.Phone
        hasVariations(
            inputType,
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        ) -> SecretType.Email
        hasVariations(
            inputType,
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            InputType.TYPE_NUMBER_VARIATION_PASSWORD
        ) -> SecretType.Password
        hasVariations(
            inputType,
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        ) -> SecretType.FullName
        else -> null
    }

    private fun hasVariations(inputType: Int, vararg variations: Int): Boolean {
        return variations.any { inputType and InputType.TYPE_MASK_VARIATION == it }
    }
}

fun AssistStructure.ViewNode.toAutofillNode(): AutofillNode = AutofillNode(
    autofillId?.let(::AndroidAutofillFieldId),
    className,
    isImportantForAutofill(this),
    text?.toString(),
    autofillValue,
    inputType,
    autofillHints?.toList().orEmpty(),
    htmlInfo?.attributes?.toList()?.map { it.first to it.second }.orEmpty(),
    (0 until childCount).map { getChildAt(it).toAutofillNode() }
)

private fun isImportantForAutofill(node: AssistStructure.ViewNode): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        when (node.importantForAutofill) {
            View.IMPORTANT_FOR_AUTOFILL_AUTO,
            View.IMPORTANT_FOR_AUTOFILL_YES,
            View.IMPORTANT_FOR_AUTOFILL_YES_EXCLUDE_DESCENDANTS -> true
            else -> false
        }
    } else {
        true
    }

private infix fun Int.hasFlag(flag: Int): Boolean = this and flag == flag
