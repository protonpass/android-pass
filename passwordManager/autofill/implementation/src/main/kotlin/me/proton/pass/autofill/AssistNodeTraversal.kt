package me.proton.pass.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.text.InputType
import android.view.View
import me.proton.pass.autofill.entities.AndroidAutofillFieldId
import me.proton.pass.autofill.entities.AssistField
import me.proton.pass.autofill.entities.AutofillNode
import me.proton.pass.autofill.entities.FieldType
import me.proton.pass.autofill.entities.InputTypeValue
import me.proton.core.util.kotlin.hasFlag

class AssistNodeTraversal {

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
        val hasAutofillInfo = nodeHasValidHints(node.autofillHints.toSet()) ||
            nodeHasValidHtmlInfo(node.htmlAttributes) ||
            nodeHasValidInputType(node)

        return node.id != null && hasAutofillInfo && isImportant
    }

    private fun nodeHasValidHints(autofillHints: Set<String>): Boolean = autofillHints
        .firstOrNull()
        .let {
            if (it != null) {
                detectFieldTypeUsingAutofillHint(it) != FieldType.Unknown
            } else {
                false
            }
        }

    private fun nodeHasValidHtmlInfo(htmlAttributes: List<Pair<String, String>>): Boolean =
        detectFieldTypeUsingHtmlInfo(htmlAttributes) != FieldType.Unknown

    private fun nodeHasValidInputType(node: AutofillNode): Boolean {
        val hasMultilineFlag = node.inputType.value.hasFlag(InputType.TYPE_TEXT_FLAG_MULTI_LINE) ||
            node.inputType.value.hasFlag(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
        val hasAutoCorrectFlag = node.inputType.value.hasFlag(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
        // InputType.TYPE_TEXT_FLAG_CAP_SENTENCES might also be considered in the future
        return !hasMultilineFlag &&
            !hasAutoCorrectFlag &&
            detectFieldTypeUsingInputType(node.inputType) != FieldType.Unknown
    }

    private fun detectFieldType(node: AutofillNode): FieldType {
        val autofillHint = node.autofillHints.firstOrNull()
        val htmlAttributes = node.htmlAttributes
        var fieldType: FieldType = FieldType.Unknown
        if (autofillHint != null) {
            fieldType = detectFieldTypeUsingAutofillHint(autofillHint)
        }
        if (fieldType == FieldType.Unknown && htmlAttributes.isNotEmpty()) {
            fieldType = detectFieldTypeUsingHtmlInfo(htmlAttributes)
        }
        if (fieldType == FieldType.Unknown) {
            fieldType = detectFieldTypeUsingInputType(node.inputType)
        }
        return fieldType
    }

    fun detectFieldTypeUsingHtmlInfo(
        htmlAttributes: List<Pair<String, String>>
    ): FieldType {
        val typeAttribute = htmlAttributes.firstOrNull { it.first == "type" }
        return when (typeAttribute?.second) {
            "tel" -> FieldType.Phone
            "email" -> FieldType.Email
            "password" -> FieldType.Password
            // Forms tend to contain this field, commented to see if we have many issues without it
            // "text" -> FieldType.Other
            else -> FieldType.Unknown
        }
    }

    fun detectFieldTypeUsingAutofillHint(autofillHint: String): FieldType = when (autofillHint) {
        View.AUTOFILL_HINT_EMAIL_ADDRESS -> FieldType.Email
        View.AUTOFILL_HINT_NAME -> FieldType.FullName
        View.AUTOFILL_HINT_USERNAME -> FieldType.Username
        View.AUTOFILL_HINT_PASSWORD, HINT_CURRENT_PASSWORD -> FieldType.Password
        View.AUTOFILL_HINT_PHONE -> FieldType.Phone
        else -> FieldType.Unknown
    }

    fun detectFieldTypeUsingInputType(inputType: InputTypeValue): FieldType = when {
        inputType.value == InputType.TYPE_CLASS_PHONE -> FieldType.Phone
        inputType.hasVariations(
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        ) -> FieldType.Email
        /*
        Taken from keepass, we have to either provide the pass type or the username
        inputType.hasVariations(
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        ) -> FieldType.Username */
        inputType.hasVariations(
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        ) -> FieldType.Password
        inputType.hasVariations(
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        ) -> FieldType.FullName
        else -> FieldType.Unknown
    }

    companion object {
        const val HINT_CURRENT_PASSWORD = "current-password"
    }
}

fun AssistStructure.ViewNode.toAutofillNode(): AutofillNode = AutofillNode(
    id = autofillId?.let(::AndroidAutofillFieldId),
    className = className,
    isImportantForAutofill = isImportantForAutofill(this),
    text = text?.toString(),
    autofillValue = autofillValue,
    inputType = InputTypeValue(inputType),
    autofillHints = autofillHints?.toList().orEmpty(),
    htmlAttributes = htmlInfo?.attributes?.toList()?.map { it.first to it.second }.orEmpty(),
    children = (0 until childCount).map { getChildAt(it).toAutofillNode() }
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
