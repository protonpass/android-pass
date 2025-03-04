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

package proton.android.pass.commonpresentation.impl.items.details.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemCustomFieldSection
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.attachments.Attachment
import javax.inject.Inject

class SSHKeyDetailsHandlerObserverImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : ItemDetailsHandlerObserver<ItemContents.SSHKey>() {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState
    ): Flow<ItemDetailState> = observeCustomItemContents(item)
        .mapLatest { identityItemContents ->
            ItemDetailState.SSHKey(
                itemContents = identityItemContents,
                itemId = item.id,
                shareId = item.shareId,
                isItemPinned = item.isPinned,
                itemCreatedAt = item.createTime,
                itemModifiedAt = item.modificationTime,
                itemLastAutofillAtOption = item.lastAutofillTime,
                itemRevision = item.revision,
                itemState = ItemState.from(item.state),
                itemDiffs = ItemDiffs.SSHKey(),
                itemShare = share,
                itemShareCount = item.shareCount,
                attachmentsState = attachmentsState
            )
        }

    private fun observeCustomItemContents(item: Item): Flow<ItemContents.SSHKey> = flow {
        encryptionContextProvider.withEncryptionContext {
            toItemContents(
                itemType = item.itemType,
                encryptionContext = this,
                title = item.title,
                note = item.note,
                flags = item.flags
            ) as ItemContents.SSHKey
        }.let { identityItemContents ->
            emit(identityItemContents)
        }
    }

    @Suppress("LongMethod")
    override fun updateItemContents(
        itemContents: ItemContents.SSHKey,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        hiddenFieldSection: ItemCustomFieldSection,
        hiddenState: HiddenState
    ): ItemContents = when (hiddenFieldType) {
        is ItemDetailsFieldType.Hidden.CustomField -> {
            when (hiddenFieldSection) {
                is ItemCustomFieldSection.ExtraSection -> itemContents.copy(
                    sectionContentList = itemContents.sectionContentList
                        .toMutableList()
                        .apply {
                            itemContents.sectionContentList[hiddenFieldSection.index]
                                .let { extraSectionContent ->
                                    set(
                                        index = hiddenFieldSection.index,
                                        element = extraSectionContent.copy(
                                            customFieldList = toggleHiddenCustomField(
                                                customFieldsContent = extraSectionContent.customFieldList,
                                                hiddenFieldType = hiddenFieldType,
                                                hiddenState = hiddenState
                                            )
                                        )
                                    )
                                }
                        }
                )
                is ItemCustomFieldSection.CustomField -> itemContents.copy(
                    customFieldList = toggleHiddenCustomField(
                        customFieldsContent = itemContents.customFieldList,
                        hiddenFieldType = hiddenFieldType,
                        hiddenState = hiddenState
                    )
                )
                is ItemCustomFieldSection.Identity -> itemContents
            }
        }

        ItemDetailsFieldType.Hidden.Cvv,
        ItemDetailsFieldType.Hidden.Password,
        ItemDetailsFieldType.Hidden.Pin -> itemContents
    }

    @Suppress("LongMethod")
    override fun calculateItemDiffs(
        baseItemContents: ItemContents.SSHKey,
        otherItemContents: ItemContents.SSHKey,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.SSHKey(
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
                baseItemCustomFieldsContent = baseItemContents.customFieldList,
                otherItemCustomFieldsContent = otherItemContents.customFieldList
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

}
