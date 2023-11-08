/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.text.InputType
import android.view.View
import me.proton.core.util.kotlin.hasFlag
import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AssistInfo
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.AutofillNode
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.entities.InputTypeValue
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.log.api.PassLogger

class AssistNodeTraversal {

    private var autoFillNodes = mutableListOf<AssistField>()
    private var detectedUrl: Option<String> = None

    private val usernameKeywords = listOf(
        View.AUTOFILL_HINT_USERNAME,
        View.AUTOFILL_HINT_EMAIL_ADDRESS,
        "email",
        "username",
        "user name",
        "identifier",
        "account_name"
    )

    // For testing purposes
    var visitedNodes = 0
        private set

    fun traverse(node: AssistStructure.ViewNode): AssistInfo =
        traverse(node.toAutofillNode())

    fun traverse(node: AutofillNode): AssistInfo {
        visitedNodes = 0
        autoFillNodes = mutableListOf()
        traverseInternal(node, emptyList())
        return AssistInfo(
            fields = autoFillNodes,
            url = detectedUrl
        )
    }

    private fun traverseInternal(node: AutofillNode, parentPath: List<AutofillFieldId>) {
        if (detectedUrl is None) {
            detectedUrl = node.url
        }

        val pathToCurrentNode = parentPath.toMutableList().apply { add(node.id!!) }.toList()
        if (nodeSupportsAutoFill(node)) {
            val assistField = AssistField(
                id = node.id!!,
                type = detectFieldType(node),
                value = node.autofillValue,
                text = node.text.toString(),
                isFocused = node.isFocused,
                nodePath = pathToCurrentNode,
            )
            autoFillNodes.add(assistField)
        }

        node.children.forEach {
            traverseInternal(it, pathToCurrentNode)
        }

        visitedNodes += 1
    }

    private fun nodeSupportsAutoFill(node: AutofillNode): Boolean {
        val isImportant = node.isImportantForAutofill
        val hasAutofillInfo = nodeHasValidHints(node.autofillHints.toSet()) ||
            nodeHasValidHtmlInfo(node.htmlAttributes) ||
            nodeHasValidInputType(node)

        if (node.className == "android.widget.EditText") {
            PassLogger.d(TAG, "------------------------------------")
            PassLogger.d(TAG, "nodeInputTypeFlags ${InputTypeFlags.fromValue(node.inputType)}")
            PassLogger.d(TAG, "nodeSupportsAutoFill $isImportant - $hasAutofillInfo")
            PassLogger.d(TAG, "nodeHasValidHints ${nodeHasValidHints(node.autofillHints.toSet())}")
            PassLogger.d(TAG, "nodeHasValidHtmlInfo ${nodeHasValidHtmlInfo(node.htmlAttributes)}")
            PassLogger.d(TAG, "nodeHasValidInputType ${nodeHasValidInputType(node)}")
            PassLogger.d(TAG, "------------------------------------")
        }

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
            (
                detectFieldTypeUsingInputType(node.inputType) != FieldType.Unknown ||
                    detectFieldTypeUsingHintKeywordList(node.hintKeywordList) != FieldType.Unknown
                )
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
        if (fieldType == FieldType.Unknown) {
            fieldType = detectFieldTypeUsingHintKeywordList(node.hintKeywordList)
        }

        return fieldType
    }

    private fun detectFieldTypeUsingHtmlInfo(
        htmlAttributes: List<Pair<String, String>>
    ): FieldType {
        val typeAttribute = htmlAttributes.firstOrNull { it.first == "type" }
        return when (typeAttribute?.second) {
            "email" -> FieldType.Email
            "password" -> FieldType.Password
            // Support for these fields will be added in the future
            // "tel" -> FieldType.Phone
            // "text" -> FieldType.Other
            else -> FieldType.Unknown
        }
    }

    @Suppress("ReturnCount")
    fun detectFieldTypeUsingAutofillHint(autofillHint: String): FieldType {
        when (autofillHint) {
            View.AUTOFILL_HINT_EMAIL_ADDRESS -> return FieldType.Email
            View.AUTOFILL_HINT_USERNAME -> return FieldType.Username
            View.AUTOFILL_HINT_PASSWORD, HINT_CURRENT_PASSWORD -> return FieldType.Password
            // Support for these fields will be added in the future
            // View.AUTOFILL_HINT_PHONE -> return FieldType.Phone
            // View.AUTOFILL_HINT_NAME -> return FieldType.FullName
        }

        if (autofillHint.lowercase().contains("username")) {
            return FieldType.Username
        }

        if (autofillHint.lowercase().contains("email")) {
            return FieldType.Email
        }

        if (autofillHint.lowercase().contains("password")) {
            return FieldType.Password
        }

        return FieldType.Unknown
    }

    private fun detectFieldTypeUsingHintKeywordList(hintKeywordList: List<CharSequence>): FieldType {
        val normalizedKeywords = hintKeywordList.map { it.toString().lowercase() }
        if (usernameKeywords.any { normalizedKeywords.contains(it) }) return FieldType.Email
        return FieldType.Unknown
    }

    private fun detectFieldTypeUsingInputType(inputType: InputTypeValue): FieldType = when {
        inputType.hasVariations(
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        ) -> FieldType.Email

        inputType.hasVariations(
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        ) -> FieldType.Password
        /* Support for these fields will be added in the future
        inputType.hasVariations(
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        ) -> FieldType.FullName
        inputType.value == InputType.TYPE_CLASS_PHONE -> FieldType.Phone
        inputType.hasVariations(
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        ) -> FieldType.Username
         */
        else -> FieldType.Unknown
    }

    companion object {
        const val HINT_CURRENT_PASSWORD = "current-password"
        const val TAG = "AssistNodeTraversal"
    }
}

fun AssistStructure.ViewNode.toAutofillNode(): AutofillNode {
    val hintKeywordList = buildList {
        add(text)
        add(idEntry)
        add(hint)
        addAll(autofillOptions?.toList() ?: emptyList<String>())
    }.filterNotNull()
    return AutofillNode(
        id = autofillId?.let(::AndroidAutofillFieldId),
        className = className,
        isImportantForAutofill = isImportantForAutofill(this),
        text = text?.toString(),
        isFocused = isFocused,
        autofillValue = autofillValue,
        inputType = InputTypeValue(inputType),
        hintKeywordList = hintKeywordList,
        autofillHints = autofillHints?.toList().orEmpty(),
        htmlAttributes = htmlInfo?.attributes?.toList()?.map { it.first to it.second }.orEmpty(),
        children = (0 until childCount).map { getChildAt(it).toAutofillNode() },
        url = getUrl()
    )
}

private fun AssistStructure.ViewNode.getUrl(): Option<String> {
    val domain = webDomain ?: return None
    if (domain.isBlank()) return None
    val scheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        webScheme ?: "https"
    } else {
        "https"
    }

    return Some("$scheme://$domain")
}

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


enum class InputTypeFlags(val value: Int) {
    MASK_CLASS(0x0000000f),
    MASK_VARIATION(0x00000ff0),
    MASK_FLAGS(0x00fff000),
    NULL(0x00000000),
    CLASS_TEXT(0x00000001),
    TEXT_FLAG_CAP_CHARACTERS(0x00001000),
    TEXT_FLAG_CAP_WORDS(0x00002000),
    TEXT_FLAG_CAP_SENTENCES(0x00004000),
    TEXT_FLAG_AUTO_CORRECT(0x00008000),
    TEXT_FLAG_AUTO_COMPLETE(0x00010000),
    TEXT_FLAG_MULTI_LINE(0x00020000),
    TEXT_FLAG_IME_MULTI_LINE(0x00040000),
    TEXT_FLAG_NO_SUGGESTIONS(0x00080000),
    TEXT_FLAG_ENABLE_TEXT_CONVERSION_SUGGESTIONS(0x00100000),
    TEXT_VARIATION_NORMAL(0x00000000),
    TEXT_VARIATION_URI(0x00000010),
    TEXT_VARIATION_EMAIL_ADDRESS(0x00000020),
    TEXT_VARIATION_EMAIL_SUBJECT(0x00000030),
    TEXT_VARIATION_SHORT_MESSAGE(0x00000040),
    TEXT_VARIATION_LONG_MESSAGE(0x00000050),
    TEXT_VARIATION_PERSON_NAME(0x00000060),
    TEXT_VARIATION_POSTAL_ADDRESS(0x00000070),
    TEXT_VARIATION_PASSWORD(0x00000080),
    TEXT_VARIATION_VISIBLE_PASSWORD(0x00000090),
    TEXT_VARIATION_WEB_EDIT_TEXT(0x000000a0),
    TEXT_VARIATION_FILTER(0x000000b0),
    TEXT_VARIATION_PHONETIC(0x000000c0),
    TEXT_VARIATION_WEB_EMAIL_ADDRESS(0x000000d0),
    TEXT_VARIATION_WEB_PASSWORD(0x000000e0),
    CLASS_NUMBER(0x00000002),
    NUMBER_FLAG_SIGNED(0x00001000),
    NUMBER_FLAG_DECIMAL(0x00002000),
    NUMBER_VARIATION_NORMAL(0x00000000),
    NUMBER_VARIATION_PASSWORD(0x00000010),
    CLASS_PHONE(0x00000003),
    CLASS_DATETIME(0x00000004),
    DATETIME_VARIATION_NORMAL(0x00000000),
    DATETIME_VARIATION_DATE(0x00000010),
    DATETIME_VARIATION_TIME(0x00000020);

    companion object {
        fun fromValue(input: InputTypeValue): List<InputTypeFlags> = values()
            .filter { flag -> input.value.and(flag.value) == flag.value }
    }
}
