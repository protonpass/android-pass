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

package proton.android.pass.autofill.heuristics

import android.app.assist.AssistStructure
import android.os.Build
import android.text.InputType
import android.view.View
import android.view.autofill.AutofillId
import android.widget.EditText
import proton.android.pass.autofill.RequestFlags
import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.AutofillNode
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.entities.InputTypeValue
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.orRight
import proton.android.pass.common.api.removeAccents
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.log.api.PassLogger

class NodeExtractor(private val requestFlags: List<RequestFlags> = emptyList()) {

    data class ExtractionResult(
        val fields: List<AssistField>
    ) {
        fun urls(): List<String> = fields.mapNotNull { it.url }

        fun mainUrl(): Option<String> {
            val focusedField = fields.firstOrNull { it.isFocused }
            if (focusedField?.url != null) {
                return focusedField.url.some()
            }

            return fields.firstOrNull { it.url != null }?.url.toOption()
        }
    }

    private var autoFillNodes = mutableListOf<AssistField>()
    private var inCreditCardContext = false

    // For testing purposes
    var visitedNodes = 0
        private set

    fun extract(node: AssistStructure.ViewNode): ExtractionResult =
        extract(node.toAutofillNode())

    fun extract(node: AutofillNode): ExtractionResult {
        visitedNodes = 0
        autoFillNodes = mutableListOf()
        traverseInternal(
            AutofillTraversalContext(
                node = node,
                parent = None,
                siblings = emptyList(),
                parentPath = emptyList(),
                parentUrl = node.url
            )
        )
        return ExtractionResult(
            fields = autoFillNodes,
        )
    }

    private fun traverseInternal(context: AutofillTraversalContext) {
        val pathToCurrentNode = context.parentPath
            .toMutableList()
            .apply { add(context.node.id!!) }
            .toList()

        if (context.node.children.isEmpty()) {
            when (val assistField = getAssistField(context)) {
                is Some -> addNode(assistField.value)
                None -> {}
            }
        } else {
            context.node.children.forEach {
                val newContext = AutofillTraversalContext(
                    node = it,
                    parent = Some(context),
                    siblings = context.node.children,
                    parentPath = pathToCurrentNode,
                    parentUrl = context.parentUrl.orRight(context.node.url)
                )

                traverseInternal(newContext)
            }
        }

        visitedNodes += 1
    }

    private fun addNode(assistField: AssistField) {
        when (assistField.type) {
            FieldType.CardNumber -> inCreditCardContext = true
            FieldType.CardExpirationMM,
            FieldType.CardExpirationMMYY,
            FieldType.CardExpirationYY,
            FieldType.CardExpirationYYYY -> if (!inCreditCardContext) {
                PassLogger.d(TAG, "Discarding expiration field because not inCreditCardContext")
                return
            }

            else -> {}
        }

        autoFillNodes.add(assistField)
    }

    private fun getAssistField(context: AutofillTraversalContext): Option<AssistField> {
        val node = context.node
        return when (val res = nodeSupportsAutoFill(node)) {
            NodeSupportsAutofillResult.No -> None
            is NodeSupportsAutofillResult.Yes -> {
                val fieldType = res.fieldType.value() ?: detectFieldType(node)
                if (isNodeAllowed(fieldType, node)) {
                    AssistField(
                        id = node.id!!,
                        type = fieldType,
                        value = node.autofillValue,
                        text = node.text.toString(),
                        isFocused = node.isFocused,
                        nodePath = context.parentPath,
                        url = context.parentUrl.orRight(context.node.url).value()
                    ).some()
                } else {
                    None
                }
            }

            NodeSupportsAutofillResult.MaybeWithContext -> getAutofillNodeFromContext(context)
        }
    }

    @Suppress("ComplexMethod", "CyclomaticComplexMethod", "ReturnCount")
    private fun nodeSupportsAutoFill(node: AutofillNode): NodeSupportsAutofillResult {
        val isImportant =
            node.isImportantForAutofill || requestFlags.contains(RequestFlags.FLAG_MANUAL_REQUEST)

        if (nodeHasValidInputType(node) == CheckInputTypeResult.DoNotAutofill) {
            PassLogger.v(TAG, "Discarding node because CheckInputTypeResult.DoNotAutofill")
            return NodeSupportsAutofillResult.No
        }

        val hasAutofillInfo = nodeHasAutofillInfo(node)
        debugNode(node, isImportant, hasAutofillInfo)

        val isEditText = node.isEditText()
        if (node.id == null) {
            if (isEditText) {
                PassLogger.d(TAG, "Discarding node because id is null")
            }

            return NodeSupportsAutofillResult.No
        }

        if (!isImportant) {
            if (isEditText) {
                PassLogger.d(
                    TAG,
                    "[node=${node.id}] Discarding node because is not important for autofill"
                )
            }
            return NodeSupportsAutofillResult.No
        }

        return when (hasAutofillInfo) {
            // If the node doesn't have autofill info but it's an edit text, maybe we can check the context
            HasAutofillInfoResult.No -> if (isEditText) {
                val fieldType = detectFieldTypeUsingHintKeywordList(node.hintKeywordList)
                if (fieldType != FieldType.Unknown) {
                    PassLogger.d(TAG, "[node=${node.id}] Marking as Yes because hintKeyword")
                    NodeSupportsAutofillResult.Yes(fieldType.some())
                } else {
                    PassLogger.d(TAG, "[node=${node.id}] Marking as Maybe because is edit text")
                    NodeSupportsAutofillResult.MaybeWithContext
                }

            } else {
                // If the node is not an edit text, we know that we can't do anything
                NodeSupportsAutofillResult.No
            }

            HasAutofillInfoResult.Yes -> {
                if (isEditText) {
                    PassLogger.d(
                        TAG,
                        "[node=${node.id}] Accepting node because it has autofill info"
                    )
                }
                NodeSupportsAutofillResult.Yes(None)
            }

            is HasAutofillInfoResult.YesWithFieldType -> {
                if (isEditText) {
                    PassLogger.d(
                        TAG,
                        "[node=${node.id}] Accepting node because it has autofill " +
                            "info and field type ${hasAutofillInfo.fieldType}"
                    )
                }
                NodeSupportsAutofillResult.Yes(hasAutofillInfo.fieldType.some())
            }
        }
    }

    private fun debugNode(
        node: AutofillNode,
        isImportant: Boolean,
        hasAutofillInfo: HasAutofillInfoResult,
    ) {
        if (node.id == null) return
        val nodeId = node.id.value()
        if (node.isEditText()) {
            val inputTypeFlags = InputTypeFlags.fromValue(node.inputType)
            val hasValidHints = nodeHasValidHints(node.autofillHints.toSet())
            val hasValidHtmlInfo = nodeHasValidHtmlInfo(node.htmlAttributes)
            val hasValidInputType = nodeHasValidInputType(node)
            PassLogger.v(TAG, "------------------------------------")
            PassLogger.v(TAG, "[$nodeId] nodeInputTypeFlags $inputTypeFlags")
            PassLogger.v(TAG, "[$nodeId] isImportant $isImportant")
            PassLogger.v(TAG, "[$nodeId] hasAutofillInfo $hasAutofillInfo")
            PassLogger.v(TAG, "[$nodeId] nodeHasValidHints $hasValidHints")
            PassLogger.v(TAG, "[$nodeId] nodeHasValidHtmlInfo $hasValidHtmlInfo")
            PassLogger.v(TAG, "[$nodeId] nodeHasValidInputType $hasValidInputType")
            PassLogger.v(TAG, "------------------------------------")
        }
    }

    @Suppress("LongMethod")
    private fun getAutofillNodeFromContext(
        autofillContext: AutofillTraversalContext
    ): Option<AssistField> {
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

        if (hasValidHints is CheckHintsResult.Found) {
            PassLogger.d(
                TAG,
                "[node=${autofillContext.node.id}] Adding with context because it has valid hints " +
                    "[type=${hasValidHints.fieldType}]"
            )
        }

        if (hasValidHtmlInfo) {
            PassLogger.d(
                TAG,
                "[node=${autofillContext.node.id}] Adding with context because it has valid html info"
            )
        }

        if (hasUsefulKeywords) {
            PassLogger.d(
                TAG,
                "[node=${autofillContext.node.id}] Adding with context because it has useful keywords"
            )
        }

        return if (hasValidHints is CheckHintsResult.Found || hasValidHtmlInfo || hasUsefulKeywords) {
            val fieldType = when (hasValidHints) {
                is CheckHintsResult.Found -> hasValidHints.fieldType
                CheckHintsResult.NoneFound -> detectFieldType(
                    autofillHints = autofillHints,
                    htmlAttributes = htmlAttributes,
                    inputType = autofillContext.node.inputType,
                    hintKeywordList = hintKeywordList
                )
            }

            if (isNodeAllowed(fieldType, autofillContext.node)) {
                AssistField(
                    id = autofillContext.node.id!!,
                    type = fieldType,
                    value = autofillContext.node.autofillValue,
                    text = autofillContext.node.text,
                    isFocused = autofillContext.node.isFocused,
                    nodePath = autofillContext.parentPath,
                    url = autofillContext.parentUrl.orRight(autofillContext.node.url).value()
                ).some()
            } else {
                None
            }
        } else {
            PassLogger.d(
                TAG,
                "[node=${autofillContext.node.id}] Discarding because could not find contextual info"
            )
            None
        }
    }

    private fun getContextNodes(context: AutofillTraversalContext): List<AutofillNode> {
        // List that will contain the context nodes
        val contextNodes = mutableListOf<AutofillNode>()

        val isNodeAlreadyAdded = { node: AutofillNode ->
            autoFillNodes.any { it.id == node.id }
        }

        val unprocessedSiblings = context.siblings.filter { !isNodeAlreadyAdded(it) }

        // Start adding the current node siblings
        contextNodes.addAll(unprocessedSiblings)

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
                    if (!isNodeAlreadyAdded(parentNode.node)) {
                        contextNodes.add(parentNode.node)
                    }

                    // Add the parents siblings
                    val nonAddedSiblings = parentNode.siblings.filter { !isNodeAlreadyAdded(it) }
                    contextNodes.addAll(nonAddedSiblings)

                    parent = parentNode.parent
                }
            }
        }
        return contextNodes
    }

    private fun nodeHasAutofillInfo(node: AutofillNode): HasAutofillInfoResult =
        when (val hintsRes = nodeHasValidHints(node.autofillHints.toSet())) {
            is CheckHintsResult.Found -> {
                HasAutofillInfoResult.YesWithFieldType(hintsRes.fieldType)
            }

            CheckHintsResult.NoneFound -> {
                val hasHtmlInfo = nodeHasValidHtmlInfo(node.htmlAttributes)
                when (val hasValidInputType = nodeHasValidInputType(node)) {
                    is CheckInputTypeResult.Found -> {
                        HasAutofillInfoResult.YesWithFieldType(hasValidInputType.fieldType)
                    }

                    CheckInputTypeResult.DoNotAutofill -> HasAutofillInfoResult.No

                    CheckInputTypeResult.NoneFound -> if (hasHtmlInfo) {
                        HasAutofillInfoResult.Yes
                    } else {
                        HasAutofillInfoResult.No
                    }
                }
            }
        }


    private fun nodeHasValidHints(autofillHints: Set<String>): CheckHintsResult {
        for (hint in autofillHints) {
            val fieldType = detectFieldTypeUsingAutofillHint(hint)
            if (fieldType != FieldType.Unknown) {
                return CheckHintsResult.Found(fieldType)
            }
        }

        return CheckHintsResult.NoneFound
    }

    private fun nodeHasValidHtmlInfo(htmlAttributes: List<Pair<String, String>>): Boolean =
        detectFieldTypeUsingHtmlInfo(htmlAttributes) != FieldType.Unknown

    @Suppress("ReturnCount")
    private fun nodeHasValidInputType(node: AutofillNode): CheckInputTypeResult {
        val flags = InputTypeFlags.fromValue(node.inputType)
        val hasMultilineFlag = flags.contains(InputTypeFlags.TEXT_FLAG_MULTI_LINE) ||
            flags.contains(InputTypeFlags.TEXT_FLAG_IME_MULTI_LINE)
        val hasAutoCorrectFlag = flags.contains(InputTypeFlags.TEXT_FLAG_AUTO_CORRECT)
        // InputTypeFlags.TYPE_TEXT_FLAG_CAP_SENTENCES might also be considered in the future

        if (hasMultilineFlag || hasAutoCorrectFlag) return CheckInputTypeResult.NoneFound

        return when (val fieldTypeByInputType = detectFieldTypeUsingInputType(node.inputType)) {
            FieldType.Unknown -> CheckInputTypeResult.NoneFound
            else -> CheckInputTypeResult.Found(fieldTypeByInputType)
        }
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

    @Suppress("ReturnCount")
    private fun detectFieldTypeUsingHtmlInfo(attributes: List<Pair<String, String>>): FieldType {
        val typeAttribute = attributes.firstOrNull { it.first == "type" }
        when (typeAttribute?.second) {
            "email" -> return FieldType.Email
            "password" -> return FieldType.Password
            "submit" -> return FieldType.Unknown
            // Support for these fields will be added in the future
            // "tel" -> FieldType.Phone
            // "text" -> FieldType.Other
            else -> {}
        }

        val htmlValues = attributes.map { it.second }.map(::sanitizeHint)

        val (fieldType, match) = fieldKeywordsList.match(htmlValues)
        if (fieldType != FieldType.Unknown) {
            PassLogger.v(TAG, "Found field type $fieldType using html attr $match")
        }
        return fieldType
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

    @Suppress("ComplexMethod", "CyclomaticComplexMethod", "ReturnCount")
    fun detectFieldTypeUsingAutofillHint(hint: String): FieldType {
        when (hint) {
            View.AUTOFILL_HINT_EMAIL_ADDRESS -> return FieldType.Email
            View.AUTOFILL_HINT_USERNAME -> return FieldType.Username
            View.AUTOFILL_HINT_PASSWORD, HINT_CURRENT_PASSWORD -> return FieldType.Password
            View.AUTOFILL_HINT_NAME -> return FieldType.CardholderName
            View.AUTOFILL_HINT_CREDIT_CARD_NUMBER -> return FieldType.CardNumber
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH -> return FieldType.CardExpirationMM
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR -> return FieldType.CardExpirationYY
            View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE -> return FieldType.CardCvv
            else -> {}
        }

        val sanitizedHint = sanitizeHint(hint)

        if (USERNAME_REGEX.containsMatchIn(sanitizedHint)) return FieldType.Username
        if (EMAIL_REGEX.containsMatchIn(sanitizedHint)) return FieldType.Email

        return fieldKeywordsList.match(sanitizedHint).also {
            if (it != FieldType.Unknown) {
                PassLogger.v(TAG, "Found field type $it using hint $hint")
            }
        }
    }

    private fun sanitizeHint(hint: String): String = hint.lowercase()
        .replace("-", "")
        .replace("_", "")
        .replace("/", "")
        .replace(" ", "")
        .removeAccents()

    private fun detectFieldTypeUsingHintKeywordList(hintKeywordList: List<CharSequence>): FieldType {
        val normalizedKeywords = hintKeywordList.map(CharSequence::toString).map(this::sanitizeHint)

        for (kw in normalizedKeywords) {
            when (val type = detectFieldTypeUsingAutofillHint(kw)) {
                FieldType.Unknown -> {}
                else -> return type
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

    private fun isNodeAllowed(fieldType: FieldType, node: AutofillNode): Boolean {
        val fieldTypeConfig = fieldKeywordsList.firstOrNull { it.fieldType == fieldType }
            ?: return true

        val nodeHints = node.autofillHints + node.hintKeywordList
        for (nodeHint in nodeHints) {
            for (deniedKeyword in fieldTypeConfig.deniedKeywords) {
                if (nodeHint.contains(deniedKeyword)) {
                    PassLogger.d(
                        TAG,
                        "[node=${node.id?.value()}] Denied node because contains deniedKeyword [$deniedKeyword]"
                    )
                    return false
                }
            }
        }

        val inputTypeFlags = InputTypeFlags.fromValue(node.inputType)
        return !inputTypeFlags.contains(InputTypeFlags.TEXT_FLAG_MULTI_LINE)
    }

    sealed interface CheckHintsResult {
        object NoneFound : CheckHintsResult

        @JvmInline
        value class Found(val fieldType: FieldType) : CheckHintsResult
    }

    sealed interface CheckInputTypeResult {
        object NoneFound : CheckInputTypeResult
        object DoNotAutofill : CheckInputTypeResult

        @JvmInline
        value class Found(val fieldType: FieldType) : CheckInputTypeResult
    }

    sealed interface HasAutofillInfoResult {
        object No : HasAutofillInfoResult
        object Yes : HasAutofillInfoResult

        @JvmInline
        value class YesWithFieldType(val fieldType: FieldType) : HasAutofillInfoResult
    }

    sealed interface NodeSupportsAutofillResult {
        object No : NodeSupportsAutofillResult

        @JvmInline
        value class Yes(val fieldType: Option<FieldType>) : NodeSupportsAutofillResult
        object MaybeWithContext : NodeSupportsAutofillResult
    }

    data class AutofillTraversalContext(
        val node: AutofillNode,
        val parent: Option<AutofillTraversalContext>,
        val siblings: List<AutofillNode>,
        val parentPath: List<AutofillFieldId>,
        val parentUrl: Option<String>
    )

    companion object {
        const val HINT_CURRENT_PASSWORD = "current-password"
        const val TAG = "AssistNodeTraversal"
        const val MAX_CONTEXT_JUMPS = 3


        private val REGEX_OPTIONS = setOf(RegexOption.IGNORE_CASE)

        // Regexes extracted from the internal autofill repo that web uses for field detection.
        // Path: src/dictionary/generated/dictionary.ts
        @Suppress("MaxLineLength")
        private val USERNAME_REGEX = Regex(
            "(?:(?:n(?:ouvelleses|uevase|ewses)s|iniciarses|connex)io|anmeldedate|sign[io])n|in(?:iciarsessao|troduce)|a(?:uthenticate|nmeld(?:ung|en))|authentifier|s(?:econnect|identifi)er|novasessao|(?:introduci|conecta|entr[ae])r|prihlasit|connect|acceder|login",
            REGEX_OPTIONS
        )
        private val EMAIL_REGEX = Regex("co(?:urriel|rrei?o)|email", REGEX_OPTIONS)
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
        autofillHints = autofillHints?.toList()?.filter { it != null && it.isNotBlank() }.orEmpty(),
        htmlAttributes = (htmlInfo?.attributes?.toList() ?: emptyList())
            .filter { it.first != null && it.second != null }
            .filter { it.first.isNotBlank() && it.second.isNotBlank() }
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

fun AssistStructure.ViewNode.findChildById(id: AutofillId): AssistStructure.ViewNode? {
    if (autofillId == id) return this
    for (i in 0 until childCount) {
        val child = getChildAt(i).findChildById(id)
        if (child != null) return child
    }
    return null
}
