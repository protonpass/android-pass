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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.Totp
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class WifiNetworkDetailsHandlerObserverImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val totpManager: TotpManager
) : ItemDetailsHandlerObserver<ItemContents.WifiNetwork>() {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState
    ): Flow<ItemDetailState> = combine(
        observeWifiNetworkItemContents(item),
        observeTotps(item)
    ) { itemContents, customFieldsTotps ->
        ItemDetailState.WifiNetwork(
            itemContents = itemContents,
            itemId = item.id,
            shareId = item.shareId,
            isItemPinned = item.isPinned,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            itemLastAutofillAtOption = item.lastAutofillTime,
            itemRevision = item.revision,
            itemState = ItemState.from(item.state),
            itemDiffs = ItemDiffs.WifiNetwork(),
            itemShare = share,
            itemShareCount = item.shareCount,
            attachmentsState = attachmentsState,
            customFieldTotps = customFieldsTotps
        )
    }

    private fun observeWifiNetworkItemContents(item: Item): Flow<ItemContents.WifiNetwork> = flow {
        encryptionContextProvider.withEncryptionContext {
            toItemContents(
                itemType = item.itemType,
                encryptionContext = this,
                title = item.title,
                note = item.note,
                flags = item.flags
            ) as ItemContents.WifiNetwork
        }.let { identityItemContents ->
            emit(identityItemContents)
        }
    }

    private fun observeTotps(item: Item): Flow<Map<Pair<Option<Int>, Int>, Totp>> =
        observeWifiNetworkItemContents(item).flatMapLatest { contents ->
            val decrypted = encryptionContextProvider.withEncryptionContextSuspendable {
                val sectionCustomFields =
                    contents.sectionContentList.flatMapIndexed { sectionIndex, sectionContent ->
                        sectionContent.customFieldList.mapToDecryptedTotp(
                            sectionIndex = sectionIndex.some(),
                            decrypt = ::decrypt
                        )
                    }.toMap()

                val customFields = contents.customFieldList.mapToDecryptedTotp(
                    sectionIndex = None,
                    decrypt = ::decrypt
                ).toMap()

                sectionCustomFields + customFields
            }
            val flows = decrypted.map { uri ->
                totpManager.observeCode(uri.value)
                    .map {
                        uri.key to Totp(
                            code = it.code,
                            remainingSeconds = it.remainingSeconds,
                            totalSeconds = it.totalSeconds
                        )
                    }
            }
            combine(flows) { it.toMap() }
        }.onStart { emit(emptyMap()) }

    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.WifiNetwork,
        revealedHiddenFields: Map<ItemSection, Set<ItemDetailsFieldType.Hidden>>
    ): ItemContents {
        val revealedFields = revealedHiddenFields[ItemSection.WifiNetwork] ?: emptyList()
        val mutableSections = itemContents.sectionContentList.toMutableList()
        val mutableCustomFields = itemContents.customFieldList.toMutableList()

        mutableSections.forEachIndexed { sectionIndex, sectionContent ->
            val updatedCustomFields = sectionContent.customFieldList.mapIndexed { fieldIndex, field ->
                val shouldBeRevealed = revealedHiddenFields[ItemSection.ExtraSection(sectionIndex)]
                    ?.any { it is ItemDetailsFieldType.Hidden.CustomField && it.index == fieldIndex } == true
                updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
            }
            mutableSections[sectionIndex] = sectionContent.copy(customFieldList = updatedCustomFields)
        }

        mutableCustomFields.forEachIndexed { index, field ->
            val shouldBeRevealed = revealedHiddenFields[ItemSection.CustomField]
                ?.any { it is ItemDetailsFieldType.Hidden.CustomField && it.index == index } == true
            mutableCustomFields[index] = updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
        }

        return itemContents.copy(
            password = updateHiddenStateValue(
                hiddenState = itemContents.password,
                shouldBeRevealed = revealedFields.contains(ItemDetailsFieldType.Hidden.Password),
                encryptionContextProvider = encryptionContextProvider
            ),
            sectionContentList = mutableSections,
            customFieldList = mutableCustomFields
        )
    }

    @Suppress("LongMethod")
    override fun calculateItemDiffs(
        baseItemContents: ItemContents.WifiNetwork,
        otherItemContents: ItemContents.WifiNetwork,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.WifiNetwork(
            title = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.title,
                otherItemFieldValue = otherItemContents.title
            ),
            note = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.note,
                otherItemFieldValue = otherItemContents.note
            ),
            ssid = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.ssid,
                otherItemFieldValue = otherItemContents.ssid
            ),
            password = calculateItemDiffType(
                encryptionContext = this@withEncryptionContext,
                baseItemFieldHiddenState = baseItemContents.password,
                otherItemFieldHiddenState = otherItemContents.password
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
