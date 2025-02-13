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
import android.widget.AutoCompleteTextView
import android.widget.EditText
import proton.android.pass.autofill.RequestFlags
import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.AutofillNode
import proton.android.pass.autofill.entities.DetectionType
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
import kotlin.math.absoluteValue

typealias Level = Int
typealias Proximity = Int

@Suppress("LargeClass")
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
    private var detectedUrl: Option<String> = None
    private var inCreditCardContext = false

    private val proximityComparator = Comparator<Int> { a, b ->
        val absCompare = a.absoluteValue.compareTo(b.absoluteValue)
        if (absCompare != 0) {
            absCompare
        } else {
            a.compareTo(b)
        }
    }

    // For testing purposes
    var visitedNodes = 0
        private set

    fun extract(node: AssistStructure.ViewNode): ExtractionResult = extract(node.toAutofillNode())

    fun extract(node: AutofillNode): ExtractionResult {
        visitedNodes = 0
        autoFillNodes = mutableListOf()
        traverseInternal(
            AutofillTraversalContext(
                node = node,
                parent = None,
                siblings = emptyMap(),
                parentPath = emptyList(),
                parentUrl = node.url
            )
        )

        if (detectedUrl.isNotEmpty()) {
            autoFillNodes = autoFillNodes.map {
                if (it.url == null) {
                    it.copy(url = detectedUrl.value())
                } else {
                    it
                }
            }.toMutableList()
        }

        return ExtractionResult(
            fields = autoFillNodes
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

        if (!hasChildren(context.node)) {
            when (val assistField = getAssistField(context)) {
                is Some -> addNode(assistField.value)
                None -> {}
            }
        } else {
            context.node.children.forEachIndexed { position, it ->
                val proximityMap = getElementsByProximity(context.node.children, position)
                val newContext = AutofillTraversalContext(
                    node = it,
                    parent = Some(context),
                    siblings = proximityMap,
                    parentPath = pathToCurrentNode,
                    parentUrl = context.parentUrl.orRight(context.node.url)
                )

                traverseInternal(newContext)
            }
        }

        visitedNodes += 1
    }

    private fun hasChildren(node: AutofillNode): Boolean {
        if (node.children.size == 1) {
            val child = node.children.first()
            if (child.className == "android.widget.TextView" && isNodeEmpty(child)) {
                return false
            }
        }

        return node.children.isNotEmpty()
    }

    private fun isNodeEmpty(node: AutofillNode): Boolean = if (node.children.isEmpty()) {
        node.text.isNullOrBlank() && node.autofillHints.all { it.isBlank() }
    } else {
        false
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
                        detectionType = DetectionType.ExactMatch,
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

        val isSupportedInput = node.isSupportedInput()
        if (node.id == null) {
            if (isSupportedInput) {
                PassLogger.d(TAG, "Discarding node because id is null")
            }

            return NodeSupportsAutofillResult.No
        }

        if (!isImportant && !isSupportedInput) {
            return NodeSupportsAutofillResult.No
        }

        return when (hasAutofillInfo) {
            // If the node doesn't have autofill info but it's a supported input, maybe we can check the context
            HasAutofillInfoResult.No -> if (isSupportedInput) {
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
                if (isSupportedInput) {
                    PassLogger.d(
                        TAG,
                        "[node=${node.id}] Accepting node because it has autofill info"
                    )
                }
                NodeSupportsAutofillResult.Yes(None)
            }

            is HasAutofillInfoResult.YesWithFieldType -> {
                if (isSupportedInput) {
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
        hasAutofillInfo: HasAutofillInfoResult
    ) {
        if (node.id == null) return
        val nodeId = node.id.value()
        if (node.isSupportedInput()) {
            val inputTypeFlags = InputTypeFlags.fromValue(node.inputType)
            val hasValidHints = nodeHasValidHints(node.autofillHints.toSet())
            val hasValidHtmlInfo = nodeHasValidHtmlInfo(node.htmlAttributes)
            val hasValidInputType = nodeHasValidInputType(node)
            PassLogger.v(TAG, "------------------------------------")
            PassLogger.v(TAG, "[$nodeId] Node Input Type Flags: $inputTypeFlags")
            val htmlAttributesOutput = if (node.htmlAttributes.isEmpty()) {
                "No HTML attributes"
            } else {
                node.htmlAttributes.joinToString()
            }
            PassLogger.v(TAG, "[$nodeId] HTML Attributes: $htmlAttributesOutput")
            val hintKeywordListOutput = if (node.hintKeywordList.isEmpty()) {
                "No hint keywords"
            } else {
                node.hintKeywordList.joinToString()
            }
            PassLogger.v(TAG, "[$nodeId] Hint Keyword List: $hintKeywordListOutput")
            PassLogger.v(TAG, "[$nodeId] Is Important: $isImportant")
            PassLogger.v(TAG, "[$nodeId] Has Autofill Info: $hasAutofillInfo")
            PassLogger.v(TAG, "[$nodeId] Node Has Valid Hints: $hasValidHints")
            PassLogger.v(TAG, "[$nodeId] Node Has Valid HTML Info: $hasValidHtmlInfo")
            PassLogger.v(TAG, "[$nodeId] Node Has Valid Input Type: $hasValidInputType")
            PassLogger.v(TAG, "------------------------------------")
        }
    }

    @Suppress("LongMethod")
    private fun getAutofillNodeFromContext(autofillContext: AutofillTraversalContext): Option<AssistField> {
        // Invariant: node must be a supported input
        if (!autofillContext.node.isSupportedInput()) return None

        // Fetch the context nodes
        val contextNodes: Map<Level, Map<Proximity, List<AutofillNode>>> =
            getContextNodes(autofillContext)

        // Now that we have all the context nodes, aggregate the autofillHints and htmlAttributes lists
        val autofillHintsFlattened = contextNodes.flatMap { byLevel ->
            byLevel.value.flatMap { byProximity ->
                byProximity.value.flatMap {
                    it.autofillHints.filter { hint -> hint.isNotBlank() }
                }
            }
        }.toSet()

        val htmlAttributesFlattened = contextNodes.flatMap { byLevel ->
            byLevel.value.flatMap { byProximity ->
                byProximity.value.flatMap {
                    it.htmlAttributes.filter { attr -> attr.first.isNotBlank() && attr.second.isNotBlank() }
                }
            }
        }
        val hintKeywordListFlattened = contextNodes.flatMap { byLevel ->
            byLevel.value.flatMap { byProximity ->
                byProximity.value.flatMap {
                    it.hintKeywordList.filter { hint -> hint.isNotBlank() }
                }
            }
        }

        // Check if we can extract info from these
        val hasValidHints = nodeHasValidHints(autofillHintsFlattened.toSet())
        val hasValidHtmlInfo = nodeHasValidHtmlInfo(htmlAttributesFlattened)
        val hasUsefulKeywords =
            detectFieldTypeUsingHintKeywordList(hintKeywordListFlattened) != FieldType.Unknown

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
            PassLogger.d(
                TAG,
                "[node=${autofillContext.node.id}] hintKeywordList ${hintKeywordListFlattened.joinToString()}"
            )
        }

        return if (hasValidHints is CheckHintsResult.Found || hasValidHtmlInfo || hasUsefulKeywords) {
            val (fieldType, isContextBeforeField) = when (hasValidHints) {
                is CheckHintsResult.Found -> hasValidHints.fieldType to true
                CheckHintsResult.NoneFound -> detectContextualFieldType(
                    contextNodes = contextNodes,
                    inputType = autofillContext.node.inputType
                )
            }

            if (isNodeAllowed(fieldType, autofillContext.node)) {
                AssistField(
                    id = autofillContext.node.id!!,
                    type = fieldType,
                    detectionType = DetectionType.ContextMatch(isContextBeforeField),
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

    private fun getContextNodes(context: AutofillTraversalContext): Map<Level, Map<Proximity, List<AutofillNode>>> {
        val isNodeAlreadyAdded = { node: AutofillNode ->
            autoFillNodes.any { it.id == node.id }
        }

        val unprocessedSiblings: MutableMap<Proximity, List<AutofillNode>> = context.siblings
            .mapValues { list -> list.value.filter { !isNodeAlreadyAdded(it) } }
            .toMutableMap()

        // Start adding the current node siblings
        val contextNodes: MutableMap<Level, Map<Proximity, List<AutofillNode>>> =
            mutableMapOf(0 to unprocessedSiblings)

        // Starting from the parent do as many jumps as possible until MAX_CONTEXT_JUMPS
        var parent = context.parent
        for (level in 1..MAX_CONTEXT_JUMPS) {
            when (val localParent = parent) {
                // If we have reached the root, nothing else to do
                None -> return contextNodes

                // If we have a parent, add the parent and the siblings to the current node
                is Some -> {
                    val parentNode = localParent.value
                    val levelMap = mutableMapOf<Proximity, List<AutofillNode>>()
                    // Add the parent
                    if (!isNodeAlreadyAdded(parentNode.node)) {
                        levelMap[0] = mutableListOf(parentNode.node)
                    }

                    // Add the parents siblings
                    parentNode.siblings.forEach { (proximity, item) ->
                        val nonAddedSiblings = item.filter { !isNodeAlreadyAdded(it) }
                        levelMap[proximity + 1] = nonAddedSiblings.toMutableList()
                    }

                    contextNodes[level] = levelMap
                    parent = parentNode.parent
                }
            }
        }
        return contextNodes
    }

    @Suppress("ReturnCount")
    private fun nodeHasAutofillInfo(node: AutofillNode): HasAutofillInfoResult {
        val hasValidHints = nodeHasValidHints(node.autofillHints.toSet())
        if (hasValidHints is CheckHintsResult.Found) {
            return HasAutofillInfoResult.YesWithFieldType(hasValidHints.fieldType)
        }

        val hasValidInputType = nodeHasValidInputType(node)
        if (hasValidInputType is CheckInputTypeResult.Found) {
            return HasAutofillInfoResult.YesWithFieldType(hasValidInputType.fieldType)
        }
        if (hasValidInputType is CheckInputTypeResult.DoNotAutofill) {
            return HasAutofillInfoResult.No
        }
        val hasValidHtmlInfo = nodeHasValidHtmlInfo(node.htmlAttributes)
        if (hasValidHtmlInfo) {
            return HasAutofillInfoResult.Yes
        }

        return HasAutofillInfoResult.No
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
    private fun detectContextualFieldType(
        contextNodes: Map<Level, Map<Proximity, List<AutofillNode>>>,
        inputType: InputTypeValue
    ): Pair<FieldType, Boolean> {
        var fieldType: FieldType = FieldType.Unknown
        contextNodes.toSortedMap()
            .forEach { (level: Level, byLevel: Map<Proximity, List<AutofillNode>>) ->
                byLevel.toSortedMap(proximityComparator)
                    .forEach { (proximity: Proximity, byProximity: List<AutofillNode>) ->
                        byProximity.forEach { node ->
                            fieldType =
                                detectFieldTypeUsingAutofillHints(node.autofillHints.toSet())
                            if (fieldType != FieldType.Unknown) {
                                PassLogger.v(
                                    TAG,
                                    "Found field type $fieldType " +
                                        "using contextual [$level, $proximity] autofill hints"
                                )
                                return fieldType to (proximity < 0)
                            }
                            fieldType = detectFieldTypeUsingHtmlInfo(node.htmlAttributes)
                            if (fieldType != FieldType.Unknown) {
                                PassLogger.v(
                                    TAG,
                                    "Found field type $fieldType " +
                                        "using contextual [$level, $proximity] html info"
                                )
                                return fieldType to (proximity < 0)
                            }
                            fieldType = detectFieldTypeUsingInputType(inputType)
                            if (fieldType != FieldType.Unknown) {
                                PassLogger.v(
                                    TAG,
                                    "Found field type $fieldType " +
                                        "using contextual [$level, $proximity] input type"
                                )
                                return fieldType to (proximity < 0)
                            }
                            fieldType = detectFieldTypeUsingHintKeywordList(node.hintKeywordList)
                            if (fieldType != FieldType.Unknown) {
                                PassLogger.v(
                                    TAG,
                                    "Found field type $fieldType " +
                                        "using contextual [$level, $proximity] hint keyword list"
                                )
                                return fieldType to (proximity < 0)
                            }
                        }
                    }
            }

        return fieldType to true
    }

    @Suppress("ReturnCount")
    private fun detectFieldTypeUsingHtmlInfo(attributes: List<Pair<String, String>>): FieldType {
        attributes.ifEmpty { return FieldType.Unknown }
        val typeAttribute = attributes.firstOrNull { it.first == "type" }
        when (typeAttribute?.second) {
            "email" -> return FieldType.Email
            "password" -> return FieldType.Password
            "submit" -> return FieldType.SubmitButton
            // Support for these fields will be added in the future
            // "text" -> FieldType.Other
            else -> {}
        }

        val htmlValues = attributes.map { it.second }.map(::sanitizeHint)

        val (fieldType, match) = fieldKeywordsList.match(*htmlValues.toTypedArray())
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
        val fieldType: FieldType = when (hint) {
            View.AUTOFILL_HINT_EMAIL_ADDRESS -> FieldType.Email
            View.AUTOFILL_HINT_USERNAME -> FieldType.Username
            View.AUTOFILL_HINT_PASSWORD, HINT_CURRENT_PASSWORD -> FieldType.Password

            View.AUTOFILL_HINT_CREDIT_CARD_NUMBER -> FieldType.CardNumber
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH -> FieldType.CardExpirationMM
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR -> FieldType.CardExpirationYY
            View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE -> FieldType.CardCvv

            View.AUTOFILL_HINT_POSTAL_ADDRESS -> FieldType.Address
            View.AUTOFILL_HINT_POSTAL_CODE -> FieldType.PostalCode
            View.AUTOFILL_HINT_PHONE -> FieldType.Phone
            else -> FieldType.Unknown
        }
        if (fieldType != FieldType.Unknown) {
            PassLogger.v(TAG, "Found field type $fieldType using hint $hint")
            return fieldType
        }

        val sanitizedHint = sanitizeHint(hint)

        val userNameSanitised = sanitizedHint.takeIf { !DENIED_USERNAME_KEYWORDS.contains(sanitizedHint) }.orEmpty()
        if (USERNAME_REGEX.containsMatchIn(userNameSanitised)) return FieldType.Username
        if (EMAIL_REGEX.containsMatchIn(sanitizedHint)) return FieldType.Email
        val (fieldTypeKw, match) = fieldKeywordsList.match(sanitizedHint)
        if (fieldTypeKw != FieldType.Unknown) {
            PassLogger.v(TAG, "Found field type $fieldType using hint $match")
            return fieldTypeKw
        }
        if (FULL_NAME_REGEX.containsMatchIn(sanitizedHint)) return FieldType.FullName
        return FieldType.Unknown
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
                else -> {
                    PassLogger.v(TAG, "Found field type $type using hint keyword $kw")
                    return type
                }
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

        inputType.hasVariations(
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        ) -> FieldType.FullName

        inputType.hasVariations(
            InputType.TYPE_CLASS_PHONE
        ) -> FieldType.Phone

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

        return true
    }

    sealed interface CheckHintsResult {
        data object NoneFound : CheckHintsResult

        @JvmInline
        value class Found(val fieldType: FieldType) : CheckHintsResult
    }

    sealed interface CheckInputTypeResult {
        data object NoneFound : CheckInputTypeResult
        data object DoNotAutofill : CheckInputTypeResult

        @JvmInline
        value class Found(val fieldType: FieldType) : CheckInputTypeResult
    }

    sealed interface HasAutofillInfoResult {
        data object No : HasAutofillInfoResult
        data object Yes : HasAutofillInfoResult

        @JvmInline
        value class YesWithFieldType(val fieldType: FieldType) : HasAutofillInfoResult
    }

    sealed interface NodeSupportsAutofillResult {
        data object No : NodeSupportsAutofillResult

        @JvmInline
        value class Yes(val fieldType: Option<FieldType>) : NodeSupportsAutofillResult
        data object MaybeWithContext : NodeSupportsAutofillResult
    }

    data class AutofillTraversalContext(
        val node: AutofillNode,
        val parent: Option<AutofillTraversalContext>,
        val siblings: Map<Int, List<AutofillNode>>,
        val parentPath: List<AutofillFieldId>,
        val parentUrl: Option<String>
    )

    companion object {
        const val HINT_CURRENT_PASSWORD = "current-password"
        const val TAG = "NodeExtractor"
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
        private val FULL_NAME_REGEX =
            Regex("(?<!user)(?<!last)(name|\\b(nom(?:bre)?(?:complet)?)\\b)", REGEX_OPTIONS)
        private val ADDRESS_REGEX = Regex("nom.*rue")
    }
}

private fun <T> getElementsByProximity(list: List<T>, position: Int): Map<Int, List<T>> {
    require(position in list.indices) { "Position out of bounds" }

    return list.mapIndexed { index, element ->
        val distance = if (index < position) -(position - index) else index - position
        distance to element
    }.groupBy { it.first }
        .mapValues { entry -> entry.value.map { it.second } }
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

private fun AutofillNode.isSupportedInput() = when (className) {
    EditText::class.java.name -> true
    AutoCompleteTextView::class.java.name -> true
    else -> false
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
        fun fromValue(input: InputTypeValue): List<InputTypeFlags> = entries
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
