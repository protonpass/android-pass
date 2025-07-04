/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.commonpresentation.api.items.details.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import proton.android.pass.common.api.Option
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.Share
import proton.android.pass.domain.TotpState
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.toItemContents
import proton.android.pass.totp.api.ObserveTotpFromUri

abstract class ItemDetailsHandlerObserver<ITEM_CONTENTS : ItemContents, FIELD_TYPE : ItemDetailsFieldType>(
    open val encryptionContextProvider: EncryptionContextProvider,
    open val observeTotpFromUri: ObserveTotpFromUri,
    open val canDisplayTotp: CanDisplayTotp
) {

    abstract fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState,
        savedStateEntries: Map<String, Any?>,
        detailEvent: DetailEvent
    ): Flow<ItemDetailState>

    abstract fun updateHiddenFieldsContents(
        itemContents: ITEM_CONTENTS,
        revealedHiddenCopyableFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>
    ): ItemContents

    abstract fun calculateItemDiffs(
        baseItemContents: ITEM_CONTENTS,
        otherItemContents: ITEM_CONTENTS,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs

    abstract suspend fun performAction(fieldType: FIELD_TYPE, callback: suspend (DetailEvent) -> Unit)

    protected fun observeItemContents(item: Item): Flow<ITEM_CONTENTS> = flow {
        encryptionContextProvider.withEncryptionContext {
            item.toItemContents<ITEM_CONTENTS> { decrypt(it) }
        }.let { loginItemContents ->
            emit(loginItemContents)
        }
    }

    protected fun calculateItemDiffTypes(
        encryptionContext: EncryptionContext,
        baseItemCustomFieldsContent: List<CustomFieldContent>,
        otherItemCustomFieldsContent: List<CustomFieldContent>
    ): List<ItemDiffType> {
        val baseCustomFieldsMap = baseItemCustomFieldsContent.associateBy { it.label }
        val otherCustomFieldsMap = otherItemCustomFieldsContent.associateBy { it.label }

        return baseCustomFieldsMap.map { (baseLabel, baseContent) ->
            val otherContent = otherCustomFieldsMap[baseLabel]

            when {
                otherContent == null -> {
                    ItemDiffType.Field
                }

                baseContent is CustomFieldContent.Text && otherContent is CustomFieldContent.Text -> {
                    calculateItemDiffType(
                        baseItemFieldValue = baseContent.value,
                        otherItemFieldValue = otherContent.value
                    )
                }

                baseContent is CustomFieldContent.Hidden && otherContent is CustomFieldContent.Hidden -> {
                    calculateItemDiffType(
                        encryptionContext = encryptionContext,
                        baseItemFieldHiddenState = baseContent.value,
                        otherItemFieldHiddenState = otherContent.value
                    )
                }

                baseContent is CustomFieldContent.Totp && otherContent is CustomFieldContent.Totp -> {
                    calculateItemDiffType(
                        encryptionContext = encryptionContext,
                        baseItemFieldHiddenState = baseContent.value,
                        otherItemFieldHiddenState = otherContent.value
                    )
                }

                baseContent is CustomFieldContent.Date && otherContent is CustomFieldContent.Date -> {
                    calculateItemDiffType(
                        baseItemFieldValue = baseContent.value,
                        otherItemFieldValue = otherContent.value
                    )
                }

                else -> ItemDiffType.Content
            }
        }
    }

    protected fun calculateItemDiffTypes(
        baseItemFieldValues: List<String>,
        otherItemFieldValues: List<String>
    ): Pair<ItemDiffType, List<ItemDiffType>> = when {
        baseItemFieldValues.isEmpty() -> {
            ItemDiffType.None to emptyList()
        }

        otherItemFieldValues.isEmpty() -> {
            ItemDiffType.Field to List(baseItemFieldValues.size) { ItemDiffType.None }
        }

        else -> {
            ItemDiffType.None to baseItemFieldValues.mapIndexed { index, baseItemFieldValue ->
                calculateItemDiffType(
                    baseItemFieldValue = baseItemFieldValue,
                    otherItemFieldValue = otherItemFieldValues.getOrNull(index).orEmpty()
                )
            }
        }
    }

    protected fun calculateItemDiffType(
        encryptionContext: EncryptionContext,
        baseItemFieldHiddenState: HiddenState,
        otherItemFieldHiddenState: HiddenState
    ): ItemDiffType = with(encryptionContext) {
        decrypt(baseItemFieldHiddenState.encrypted) to decrypt(otherItemFieldHiddenState.encrypted)
    }.let { (baseItemFieldValue, otherItemFieldValue) ->
        calculateItemDiffType(baseItemFieldValue, otherItemFieldValue)
    }

    protected fun calculateItemDiffType(baseItemFieldValue: String, otherItemFieldValue: String): ItemDiffType = when {
        baseItemFieldValue.isEmpty() && otherItemFieldValue.isEmpty() -> ItemDiffType.None
        baseItemFieldValue.isNotEmpty() && otherItemFieldValue.isNotEmpty() ->
            if (baseItemFieldValue == otherItemFieldValue) {
                ItemDiffType.None
            } else {
                ItemDiffType.Content
            }

        else -> ItemDiffType.Field
    }

    protected fun calculateItemDiffType(baseItemFieldValue: Long?, otherItemFieldValue: Long?): ItemDiffType =
        if (baseItemFieldValue == otherItemFieldValue) {
            ItemDiffType.None
        } else {
            ItemDiffType.Content
        }

    protected fun calculateItemDiffType(
        baseItemAttachments: List<Attachment>,
        otherItemAttachments: List<Attachment>
    ): Map<AttachmentId, ItemDiffType> {
        val baseAttachmentsMap = baseItemAttachments.associateBy { it.id }
        val otherAttachmentsMap = otherItemAttachments.associateBy { it.id }

        val diffMap = mutableMapOf<AttachmentId, ItemDiffType>()

        baseAttachmentsMap.keys.forEach { attachmentId ->
            when {
                otherAttachmentsMap[attachmentId] == null ->
                    diffMap[attachmentId] = ItemDiffType.Field

                else -> diffMap[attachmentId] =
                    ItemDiffType.None
            }
        }
        otherAttachmentsMap.keys
            .filterNot { it in baseAttachmentsMap }
            .forEach { newAttachmentId ->
                diffMap[newAttachmentId] = ItemDiffType.Field
            }

        return diffMap
    }

    protected fun updateHiddenStateValue(
        hiddenState: HiddenState,
        shouldBeRevealed: Boolean,
        encryptionContextProvider: EncryptionContextProvider
    ): HiddenState = when {
        shouldBeRevealed && hiddenState is HiddenState.Concealed ->
            encryptionContextProvider.withEncryptionContext {
                HiddenState.Revealed(hiddenState.encrypted, decrypt(hiddenState.encrypted))
            }

        !shouldBeRevealed && hiddenState is HiddenState.Revealed ->
            HiddenState.Concealed(hiddenState.encrypted)

        else -> hiddenState
    }

    protected fun updateHiddenState(
        customField: CustomFieldContent,
        shouldBeRevealed: Boolean,
        encryptionContextProvider: EncryptionContextProvider
    ): CustomFieldContent = if (customField is CustomFieldContent.Hidden) {
        customField.copy(
            value = updateHiddenStateValue(
                customField.value,
                shouldBeRevealed,
                encryptionContextProvider
            )
        )
    } else {
        customField
    }

    protected fun updateHiddenCustomFieldContents(
        customFields: List<CustomFieldContent>,
        revealedHiddenFields: Set<ItemDetailsFieldType.HiddenCopyable>
    ): List<CustomFieldContent> {
        val mutableCustomFields = customFields.toMutableList()
        customFields.forEachIndexed { index, field ->
            val shouldBeRevealed = revealedHiddenFields
                .any { it is ItemDetailsFieldType.HiddenCopyable.CustomField && it.index == index } == true
            mutableCustomFields[index] =
                updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
        }
        return mutableCustomFields
    }

    protected fun observeCustomFieldTotps(item: Item): Flow<Map<Pair<Option<Int>, Int>, TotpState>> = combine(
        observeItemContents(item),
        canDisplayTotp(shareId = item.shareId, itemId = item.id)
    ) { contents, canDisplayTotp -> Pair(contents, canDisplayTotp) }
        .flatMapLatest { (contents, canDisplayTotp) ->
            if (canDisplayTotp) {
                val decrypted = encryptionContextProvider.withEncryptionContextSuspendable {
                    contents.customFields.mapToDecryptedTotp(decrypt = ::decrypt).toMap()
                }
                if (decrypted.isEmpty()) {
                    flowOf(emptyMap())
                } else {
                    val flows = decrypted.map { (key, uri) ->
                        if (uri.isBlank()) {
                            flowOf(key to TotpState.Empty)
                        } else {
                            observeTotpFromUri(uri)
                                .map { code ->
                                    key to TotpState.Visible(
                                        code = code.code,
                                        remainingSeconds = code.remainingSeconds,
                                        totalSeconds = code.totalSeconds
                                    )
                                }
                        }
                    }
                    combine(flows) { it.toMap() }
                }
            } else {
                flowOf(emptyMap())
            }
        }
        .onStart { emit(emptyMap()) }
}
