/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.item.details.detail.presentation.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonpresentation.api.items.details.handlers.mapToDecryptedTotp
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.TotpState
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.totp.api.ObserveTotpFromUri
import javax.inject.Inject

class CustomItemDetailsHandlerObserverImpl @Inject constructor(
    override val encryptionContextProvider: EncryptionContextProvider,
    override val observeTotpFromUri: ObserveTotpFromUri,
    override val canDisplayTotp: CanDisplayTotp
) : ItemDetailsHandlerObserver<ItemContents.Custom, ItemDetailsFieldType.CustomItemAction>(
    encryptionContextProvider = encryptionContextProvider,
    observeTotpFromUri = observeTotpFromUri,
    canDisplayTotp = canDisplayTotp
) {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState,
        savedStateEntries: Map<String, Any?>,
        detailEvent: DetailEvent
    ): Flow<ItemDetailState> = combine(
        observeItemContents(item),
        observeTotps(item)
    ) { itemContents, totps ->
        ItemDetailState.Custom(
            itemContents = itemContents,
            itemId = item.id,
            shareId = item.shareId,
            isItemPinned = item.isPinned,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            itemLastAutofillAtOption = item.lastAutofillTime,
            itemRevision = item.revision,
            itemState = ItemState.from(item.state),
            itemDiffs = ItemDiffs.Custom(),
            itemShare = share,
            itemShareCount = item.shareCount,
            attachmentsState = attachmentsState,
            customFieldTotps = totps,
            detailEvent = detailEvent
        )
    }

    private fun observeTotps(item: Item): Flow<Map<Pair<Option<Int>, Int>, TotpState>> = combine(
        observeItemContents(item),
        canDisplayTotp(shareId = item.shareId, itemId = item.id)
    ) { contents, canDisplayTotp -> Pair(contents, canDisplayTotp) }
        .flatMapLatest { (contents, canDisplayTotp) ->
            if (canDisplayTotp) {
                val decrypted = encryptionContextProvider.withEncryptionContextSuspendable {
                    val sectionCustomFields =
                        contents.sectionContentList.flatMapIndexed { sectionIndex, sectionContent ->
                            sectionContent.customFieldList.mapToDecryptedTotp(
                                sectionIndex = sectionIndex.some(),
                                decrypt = ::decrypt
                            )
                        }.toMap()

                    val customFields = contents.customFields.mapToDecryptedTotp(
                        sectionIndex = None,
                        decrypt = ::decrypt
                    ).toMap()

                    sectionCustomFields + customFields
                }
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
            } else {
                flowOf(emptyMap())
            }
        }
        .onStart { emit(emptyMap()) }

    @Suppress("LongMethod")
    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.Custom,
        revealedHiddenCopyableFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>
    ): ItemContents {
        val mutableSections = itemContents.sectionContentList.toMutableList()

        mutableSections.forEachIndexed { sectionIndex, sectionContent ->
            val updatedCustomFields = sectionContent.customFieldList.mapIndexed { fieldIndex, field ->
                val shouldBeRevealed = revealedHiddenCopyableFields[ItemSection.ExtraSection(sectionIndex)]
                    ?.any { it is ItemDetailsFieldType.HiddenCopyable.CustomField && it.index == fieldIndex } == true
                updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
            }
            mutableSections[sectionIndex] = sectionContent.copy(customFieldList = updatedCustomFields)
        }

        return itemContents.copy(
            sectionContentList = mutableSections,
            customFields = updateHiddenCustomFieldContents(
                customFields = itemContents.customFields,
                revealedHiddenFields = revealedHiddenCopyableFields[ItemSection.CustomField].orEmpty()
            )
        )
    }

    @Suppress("LongMethod")
    override fun calculateItemDiffs(
        baseItemContents: ItemContents.Custom,
        otherItemContents: ItemContents.Custom,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.Custom(
            title = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.title,
                otherItemFieldValue = otherItemContents.title
            ),
            note = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.note,
                otherItemFieldValue = otherItemContents.note
            ),
            customFields = calculateItemDiffTypes(
                encryptionContext = this@withEncryptionContext,
                baseItemCustomFieldsContent = baseItemContents.customFields,
                otherItemCustomFieldsContent = otherItemContents.customFields
            ),
            extraCustomFields = baseItemContents.sectionContentList.mapIndexed { index, extraSectionContent ->
                calculateItemDiffTypes(
                    encryptionContext = this@withEncryptionContext,
                    baseItemCustomFieldsContent = extraSectionContent.customFieldList,
                    otherItemCustomFieldsContent = otherItemContents.sectionContentList
                        .getOrNull(index)
                        ?.customFieldList
                        ?: emptyList()
                )
            },
            attachments = calculateItemDiffType(
                baseItemAttachments = baseAttachments,
                otherItemAttachments = otherAttachments
            )
        )
    }

    override suspend fun performAction(
        fieldType: ItemDetailsFieldType.CustomItemAction,
        callback: suspend (DetailEvent) -> Unit
    ) = Unit

}
