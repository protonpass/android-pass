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
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonpresentation.api.items.details.handlers.mapToDecryptedTotp
import proton.android.pass.commonrust.api.WifiNetworkQRGenerator
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

class WifiNetworkDetailsHandlerObserverImpl @Inject constructor(
    override val encryptionContextProvider: EncryptionContextProvider,
    override val observeTotpFromUri: ObserveTotpFromUri,
    override val canDisplayTotp: CanDisplayTotp,
    private val wifiNetworkQRGenerator: WifiNetworkQRGenerator
) : ItemDetailsHandlerObserver<ItemContents.WifiNetwork, ItemDetailsFieldType.WifiNetworkItemAction>(
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
            customFieldTotps = customFieldsTotps,
            svgQR = wifiNetworkQRGenerator.generateQr(
                ssid = itemContents.ssid,
                password = encryptionContextProvider.withEncryptionContextSuspendable {
                    decrypt(itemContents.password.encrypted)
                },
                wifiSecurity = itemContents.wifiSecurityType
            ).getOrNull().toOption(),
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

    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.WifiNetwork,
        revealedHiddenCopyableFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>
    ): ItemContents {
        val revealedFields = revealedHiddenCopyableFields[ItemSection.WifiNetwork] ?: emptyList()
        val mutableSections = itemContents.sectionContentList.toMutableList()
        val mutableCustomFields = itemContents.customFields.toMutableList()

        mutableSections.forEachIndexed { sectionIndex, sectionContent ->
            val updatedCustomFields = sectionContent.customFieldList.mapIndexed { fieldIndex, field ->
                val shouldBeRevealed = revealedHiddenCopyableFields[ItemSection.ExtraSection(sectionIndex)]
                    ?.any { it is ItemDetailsFieldType.HiddenCopyable.CustomField && it.index == fieldIndex } == true
                updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
            }
            mutableSections[sectionIndex] = sectionContent.copy(customFieldList = updatedCustomFields)
        }

        mutableCustomFields.forEachIndexed { index, field ->
            val shouldBeRevealed = revealedHiddenCopyableFields[ItemSection.CustomField]
                ?.any { it is ItemDetailsFieldType.HiddenCopyable.CustomField && it.index == index } == true
            mutableCustomFields[index] = updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
        }

        return itemContents.copy(
            password = updateHiddenStateValue(
                hiddenState = itemContents.password,
                shouldBeRevealed = revealedFields.contains(
                    ItemDetailsFieldType.HiddenCopyable.Password(itemContents.password)
                ),
                encryptionContextProvider = encryptionContextProvider
            ),
            sectionContentList = mutableSections,
            customFields = mutableCustomFields
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
        fieldType: ItemDetailsFieldType.WifiNetworkItemAction,
        callback: suspend (DetailEvent) -> Unit
    ) = Unit

}
