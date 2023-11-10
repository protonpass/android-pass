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
import android.widget.EditText
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
import proton.android.pass.common.api.some
import proton.android.pass.log.api.PassLogger

class AssistNodeTraversal(private val requestFlags: List<RequestFlags> = emptyList()) {

    private var autoFillNodes = mutableListOf<AssistField>()
    private var detectedUrl: Option<String> = None

    private val usernameKeywords = listOf(
        View.AUTOFILL_HINT_USERNAME,
        View.AUTOFILL_HINT_EMAIL_ADDRESS,
        "email",
        "username",
        "user name",
        "identifier",
        "account_name",
        "user_id"
    )

    // For testing purposes
    var visitedNodes = 0
        private set

    fun traverse(node: AssistStructure.ViewNode): AssistInfo =
        traverse(node.toAutofillNode())

    fun traverse(node: AutofillNode): AssistInfo {
        visitedNodes = 0
        autoFillNodes = mutableListOf()
        traverseInternal(
            AutofillTraversalContext(
                node = node,
                parent = None,
                siblings = emptyList(),
                parentPath = emptyList()
            )
        )
        return AssistInfo(
            fields = autoFillNodes,
            url = detectedUrl
        )
    }

    private fun traverseInternal(context: AutofillTraversalContext) {
        if (detectedUrl is None) {
            detectedUrl = context.node.url
        }

        val pathToCurrentNode = context.parentPath
            .toMutableList()
            .apply { add(context.node.id!!) }
            .toList()

        when (val assistField = getAssistField(context)) {
            is Some -> autoFillNodes.add(assistField.value)
            None -> {}
        }

        context.node.children.forEach {
            val newContext = AutofillTraversalContext(
                node = it,
                parent = Some(context),
                siblings = context.node.children,
                parentPath = pathToCurrentNode
            )

            traverseInternal(newContext)
        }

        visitedNodes += 1
    }

    private fun getAssistField(context: AutofillTraversalContext): Option<AssistField> {
        val node = context.node
        return when (nodeSupportsAutoFill(node)) {
            SupportsAutofillResult.No -> None
            SupportsAutofillResult.Yes -> AssistField(
                id = node.id!!,
                type = detectFieldType(node),
                value = node.autofillValue,
                text = node.text.toString(),
                isFocused = node.isFocused,
                nodePath = context.parentPath,
            ).some()

            SupportsAutofillResult.MaybeWithContext -> getAutofillNodeFromContext(context)
        }
    }

    private fun nodeSupportsAutoFill(node: AutofillNode): SupportsAutofillResult {
        val isImportant =
            node.isImportantForAutofill || requestFlags.contains(RequestFlags.FLAG_MANUAL_REQUEST)
        val hasAutofillInfo = nodeHasAutofillInfo(node)

        if (node.isEditText()) {
            PassLogger.d(TAG, "------------------------------------")
            PassLogger.d(TAG, "nodeInputTypeFlags ${InputTypeFlags.fromValue(node.inputType)}")
            PassLogger.d(TAG, "nodeSupportsAutoFill $isImportant - $hasAutofillInfo")
            PassLogger.d(TAG, "nodeHasValidHints ${nodeHasValidHints(node.autofillHints.toSet())}")
            PassLogger.d(TAG, "nodeHasValidHtmlInfo ${nodeHasValidHtmlInfo(node.htmlAttributes)}")
            PassLogger.d(TAG, "nodeHasValidInputType ${nodeHasValidInputType(node)}")
            PassLogger.d(TAG, "------------------------------------")
        }

        // If the node doesn't have an id or is not important for autofill, nothing else to do
        if (node.id == null || !isImportant) {
            return SupportsAutofillResult.No
        }

        // If the node already has autofill info, we can use it
        if (hasAutofillInfo) {
            return SupportsAutofillResult.Yes
        }

        // If the node doesn't have autofill info but it's an edit text, maybe we can check the context
        return if (node.isEditText()) {
            SupportsAutofillResult.MaybeWithContext
        } else {
            // If the node is not an edit text, we know that we can't do anything
            SupportsAutofillResult.No
        }
    }

    private fun getAutofillNodeFromContext(autofillContext: AutofillTraversalContext): Option<AssistField> {
        // Invariant: node must be an EditText
        if (!autofillContext.node.isEditText()) return None

        // Fetch the context nodes
        val contextNodes = getContextNodes(autofillContext)

        // Now that we have all the context nodes, aggregate the autofillHints and htmlAttributes lists
        val autofillHints = contextNodes.flatMap { it.autofillHints }
            .filter { it.isNotBlank() }
            .toSet()
        val htmlAttributes = contextNodes.flatMap { it.htmlAttributes }
            .filter { it.first.isNotBlank() && it.second.isNotBlank() }
        val hintKeywordList = contextNodes.flatMap { it.hintKeywordList }.filter { it.isNotBlank() }

        // Check if we can extract info from these
        val hasValidHints = nodeHasValidHints(autofillHints.toSet())
        val hasValidHtmlInfo = nodeHasValidHtmlInfo(htmlAttributes)
        val hasUsefulKeywords =
            detectFieldTypeUsingHintKeywordList(hintKeywordList) != FieldType.Unknown

        return if (hasValidHints || hasValidHtmlInfo || hasUsefulKeywords) {
            AssistField(
                id = autofillContext.node.id!!,
                type = detectFieldType(
                    autofillHints = autofillHints,
                    htmlAttributes = htmlAttributes,
                    inputType = autofillContext.node.inputType,
                    hintKeywordList = hintKeywordList
                ),
                value = autofillContext.node.autofillValue,
                text = autofillContext.node.text,
                isFocused = autofillContext.node.isFocused,
                nodePath = autofillContext.parentPath
            ).some()
        } else {
            None
        }
    }

    private fun getContextNodes(context: AutofillTraversalContext): List<AutofillNode> {
        // List that will contain the context nodes
        val contextNodes = mutableListOf<AutofillNode>()

        // Start adding the current node siblings
        contextNodes.addAll(context.siblings)

        // Starting from the parent do as many jumps as possible until MAX_CONTEXT_JUMPS
        var parent = context.parent
        repeat(MAX_CONTEXT_JUMPS) {
            when (val localParent = parent) {
                // If we have reached the root, nothing else to do
                None -> return contextNodes

                // If we have a parent, add the parent and the siblings to the current node
                is Some -> {
                    val parentNode = localParent.value

                    // Add the parent
                    contextNodes.add(parentNode.node)

                    // Add the parents siblings
                    contextNodes.addAll(parentNode.siblings)

                    parent = parentNode.parent
                }
            }
        }
        return contextNodes
    }

    private fun nodeHasAutofillInfo(node: AutofillNode): Boolean =
        nodeHasValidHints(node.autofillHints.toSet()) ||
            nodeHasValidHtmlInfo(node.htmlAttributes) ||
            nodeHasValidInputType(node)

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

    @Suppress("ReturnCount")
    private fun nodeHasValidInputType(node: AutofillNode): Boolean {
        val flags = InputTypeFlags.fromValue(node.inputType)
        val hasMultilineFlag = flags.contains(InputTypeFlags.TEXT_FLAG_MULTI_LINE) ||
            flags.contains(InputTypeFlags.TEXT_FLAG_IME_MULTI_LINE)
        val hasAutoCorrectFlag = flags.contains(InputTypeFlags.TEXT_FLAG_AUTO_CORRECT)
        // InputTypeFlags.TYPE_TEXT_FLAG_CAP_SENTENCES might also be considered in the future

        if (hasMultilineFlag || hasAutoCorrectFlag) return false

        val fieldTypeByInputType = detectFieldTypeUsingInputType(node.inputType)
        if (fieldTypeByInputType != FieldType.Unknown) return true

        return false
    }

    private fun detectFieldType(node: AutofillNode): FieldType = detectFieldType(
        autofillHints = node.autofillHints.toSet(),
        htmlAttributes = node.htmlAttributes,
        inputType = node.inputType,
        hintKeywordList = node.hintKeywordList
    )

    private fun detectFieldType(
        autofillHints: Set<String>,
        htmlAttributes: List<Pair<String, String>>,
        hintKeywordList: List<CharSequence>,
        inputType: InputTypeValue
    ): FieldType {
        var fieldType: FieldType = detectFieldTypeUsingAutofillHints(autofillHints)
        if (fieldType == FieldType.Unknown && htmlAttributes.isNotEmpty()) {
            fieldType = detectFieldTypeUsingHtmlInfo(htmlAttributes)
        }
        if (fieldType == FieldType.Unknown) {
            fieldType = detectFieldTypeUsingInputType(inputType)
        }
        if (fieldType == FieldType.Unknown) {
            fieldType = detectFieldTypeUsingHintKeywordList(hintKeywordList)
        }

        return fieldType
    }

    private fun detectFieldTypeUsingHtmlInfo(attributes: List<Pair<String, String>>): FieldType {
        val typeAttribute = attributes.firstOrNull { it.first == "type" }
        return when (typeAttribute?.second) {
            "email" -> FieldType.Email
            "password" -> FieldType.Password
            // Support for these fields will be added in the future
            // "tel" -> FieldType.Phone
            // "text" -> FieldType.Other
            else -> FieldType.Unknown
        }
    }

    private fun detectFieldTypeUsingAutofillHints(hints: Set<String>): FieldType {
        hints.forEach {
            val fieldType = detectFieldTypeUsingAutofillHint(it)
            if (fieldType != FieldType.Unknown) {
                return fieldType
            }
        }
        return FieldType.Unknown
    }

    fun detectFieldTypeUsingAutofillHint(autofillHint: String): FieldType = when (autofillHint) {
        View.AUTOFILL_HINT_EMAIL_ADDRESS -> FieldType.Email
        View.AUTOFILL_HINT_USERNAME -> FieldType.Username
        View.AUTOFILL_HINT_PASSWORD, HINT_CURRENT_PASSWORD -> FieldType.Password
        else -> {
            when {
                autofillHint.lowercase().contains("username") -> FieldType.Username
                autofillHint.lowercase().contains("email") -> FieldType.Email
                autofillHint.lowercase().contains("password") -> FieldType.Password
                else -> FieldType.Unknown
            }
        }
    }

    private fun detectFieldTypeUsingHintKeywordList(hintKeywordList: List<CharSequence>): FieldType {
        val normalizedKeywords = hintKeywordList.map { it.toString().lowercase() }

        for (kw in normalizedKeywords) {
            for (usernameKw in usernameKeywords) {
                if (kw.contains(usernameKw)) return FieldType.Username
            }
        }

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

    sealed interface SupportsAutofillResult {
        object No : SupportsAutofillResult
        object Yes : SupportsAutofillResult
        object MaybeWithContext : SupportsAutofillResult
    }

    data class AutofillTraversalContext(
        val node: AutofillNode,
        val parent: Option<AutofillTraversalContext>,
        val siblings: List<AutofillNode>,
        val parentPath: List<AutofillFieldId>
    )

    companion object {
        const val HINT_CURRENT_PASSWORD = "current-password"
        const val TAG = "AssistNodeTraversal"
        const val MAX_CONTEXT_JUMPS = 3
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
        htmlAttributes = (htmlInfo?.attributes?.toList() ?: emptyList())
            .filter { it.first != null && it.second != null }
            .map { it.first.lowercase() to it.second.lowercase() },
        children = (0 until childCount).map { getChildAt(it).toAutofillNode() },
        url = getUrl()
    )
}

private fun AutofillNode.isEditText() = className == EditText::class.java.name

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
