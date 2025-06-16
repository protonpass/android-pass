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
import proton.android.pass.common.api.combineN
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

class IdentityItemDetailsHandlerObserverImpl @Inject constructor(
    override val encryptionContextProvider: EncryptionContextProvider,
    override val observeTotpFromUri: ObserveTotpFromUri,
    override val canDisplayTotp: CanDisplayTotp
) : ItemDetailsHandlerObserver<ItemContents.Identity, ItemDetailsFieldType.IdentityItemAction>(
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
    ): Flow<ItemDetailState> = combineN(
        observeItemContents(item),
        observeTotps(item),
        observePersonalDetailTotps(item),
        observeAddressDetailTotps(item),
        observeWorkDetailTotps(item),
        observeContactDetailTotps(item)
    ) { identityItemContents, customFieldTotps, personalDetailTotps, addressDetailTotps,
        workDetailTotps, contactDetailTotps ->
        ItemDetailState.Identity(
            itemContents = identityItemContents,
            itemId = item.id,
            shareId = item.shareId,
            isItemPinned = item.isPinned,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            itemLastAutofillAtOption = item.lastAutofillTime,
            itemRevision = item.revision,
            itemState = ItemState.from(item.state),
            itemDiffs = ItemDiffs.Identity(),
            itemShare = share,
            itemShareCount = item.shareCount,
            attachmentsState = attachmentsState,
            customFieldTotps = customFieldTotps,
            detailEvent = detailEvent,
            personalDetailsTotps = personalDetailTotps,
            addressDetailsTotps = addressDetailTotps,
            contactDetailsTotps = contactDetailTotps,
            workDetailsTotps = workDetailTotps
        )
    }

    @Suppress("LongMethod")
    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.Identity,
        revealedHiddenFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>
    ): ItemContents {
        val mutableSections = itemContents.extraSectionContentList.toMutableList()
        mutableSections.forEachIndexed { sectionIndex, sectionContent ->
            val updatedCustomFields =
                sectionContent.customFieldList.mapIndexed { fieldIndex, field ->
                    val shouldBeRevealed =
                        revealedHiddenFields[ItemSection.ExtraSection(sectionIndex)]
                            ?.any {
                                it is ItemDetailsFieldType.HiddenCopyable.CustomField &&
                                    it.index == fieldIndex
                            } == true
                    updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
                }
            mutableSections[sectionIndex] =
                sectionContent.copy(customFieldList = updatedCustomFields)
        }
        return itemContents.copy(
            personalDetailsContent = itemContents.personalDetailsContent.copy(
                customFields = itemContents.personalDetailsContent.customFields.mapIndexed { fieldIndex, field ->
                    val shouldBeRevealed = revealedHiddenFields[ItemSection.Identity.Personal]
                        ?.any {
                            it is ItemDetailsFieldType.HiddenCopyable.CustomField &&
                                it.index == fieldIndex
                        } == true
                    updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
                }
            ),
            workDetailsContent = itemContents.workDetailsContent.copy(
                customFields = itemContents.workDetailsContent.customFields.mapIndexed { fieldIndex, field ->
                    val shouldBeRevealed = revealedHiddenFields[ItemSection.Identity.Work]
                        ?.any {
                            it is ItemDetailsFieldType.HiddenCopyable.CustomField &&
                                it.index == fieldIndex
                        } == true
                    updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
                }
            ),
            contactDetailsContent = itemContents.contactDetailsContent.copy(
                customFields = itemContents.contactDetailsContent.customFields.mapIndexed { fieldIndex, field ->
                    val shouldBeRevealed = revealedHiddenFields[ItemSection.Identity.Contact]
                        ?.any {
                            it is ItemDetailsFieldType.HiddenCopyable.CustomField &&
                                it.index == fieldIndex
                        } == true
                    updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
                },
                socialSecurityNumber = updateHiddenStateValue(
                    hiddenState = itemContents.contactDetailsContent.socialSecurityNumber,
                    shouldBeRevealed = revealedHiddenFields[ItemSection.Identity.SocialSecurityNumber]
                        ?.any { it is ItemDetailsFieldType.HiddenCopyable.SocialSecurityNumber } == true,
                    encryptionContextProvider = encryptionContextProvider
                )
            ),
            addressDetailsContent = itemContents.addressDetailsContent.copy(
                customFields = itemContents.addressDetailsContent.customFields.mapIndexed { fieldIndex, field ->
                    val shouldBeRevealed = revealedHiddenFields[ItemSection.Identity.Address]
                        ?.any {
                            it is ItemDetailsFieldType.HiddenCopyable.CustomField &&
                                it.index == fieldIndex
                        } == true
                    updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
                }
            ),
            extraSectionContentList = mutableSections
        )
    }

    @Suppress("LongMethod")
    override fun calculateItemDiffs(
        baseItemContents: ItemContents.Identity,
        otherItemContents: ItemContents.Identity,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.Identity(
            title = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.title,
                otherItemFieldValue = otherItemContents.title
            ),
            note = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.note,
                otherItemFieldValue = otherItemContents.note
            ),
            fullName = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.personalDetailsContent.fullName,
                otherItemFieldValue = otherItemContents.personalDetailsContent.fullName
            ),
            firstName = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.personalDetailsContent.firstName,
                otherItemFieldValue = otherItemContents.personalDetailsContent.firstName
            ),
            middleName = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.personalDetailsContent.middleName,
                otherItemFieldValue = otherItemContents.personalDetailsContent.middleName
            ),
            lastName = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.personalDetailsContent.lastName,
                otherItemFieldValue = otherItemContents.personalDetailsContent.lastName
            ),
            birthdate = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.personalDetailsContent.birthdate,
                otherItemFieldValue = otherItemContents.personalDetailsContent.birthdate
            ),
            gender = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.personalDetailsContent.gender,
                otherItemFieldValue = otherItemContents.personalDetailsContent.gender
            ),
            email = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.personalDetailsContent.email,
                otherItemFieldValue = otherItemContents.personalDetailsContent.email
            ),
            phoneNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.personalDetailsContent.phoneNumber,
                otherItemFieldValue = otherItemContents.personalDetailsContent.phoneNumber
            ),
            organization = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.addressDetailsContent.organization,
                otherItemFieldValue = otherItemContents.addressDetailsContent.organization
            ),
            streetAddress = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.addressDetailsContent.streetAddress,
                otherItemFieldValue = otherItemContents.addressDetailsContent.streetAddress
            ),
            zipOrPostalCode = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.addressDetailsContent.zipOrPostalCode,
                otherItemFieldValue = otherItemContents.addressDetailsContent.zipOrPostalCode
            ),
            city = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.addressDetailsContent.city,
                otherItemFieldValue = otherItemContents.addressDetailsContent.city
            ),
            stateOrProvince = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.addressDetailsContent.stateOrProvince,
                otherItemFieldValue = otherItemContents.addressDetailsContent.stateOrProvince
            ),
            countryOrRegion = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.addressDetailsContent.countryOrRegion,
                otherItemFieldValue = otherItemContents.addressDetailsContent.countryOrRegion
            ),
            floor = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.addressDetailsContent.floor,
                otherItemFieldValue = otherItemContents.addressDetailsContent.floor
            ),
            county = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.addressDetailsContent.county,
                otherItemFieldValue = otherItemContents.addressDetailsContent.county
            ),
            socialSecurityNumber = calculateItemDiffType(
                encryptionContext = this@withEncryptionContext,
                baseItemFieldHiddenState = baseItemContents.contactDetailsContent.socialSecurityNumber,
                otherItemFieldHiddenState = otherItemContents.contactDetailsContent.socialSecurityNumber
            ),
            passportNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.passportNumber,
                otherItemFieldValue = otherItemContents.contactDetailsContent.passportNumber
            ),
            licenseNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.licenseNumber,
                otherItemFieldValue = otherItemContents.contactDetailsContent.licenseNumber
            ),
            website = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.website,
                otherItemFieldValue = otherItemContents.contactDetailsContent.website
            ),
            xHandle = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.xHandle,
                otherItemFieldValue = otherItemContents.contactDetailsContent.xHandle
            ),
            secondPhoneNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.secondPhoneNumber,
                otherItemFieldValue = otherItemContents.contactDetailsContent.secondPhoneNumber
            ),
            linkedin = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.linkedin,
                otherItemFieldValue = otherItemContents.contactDetailsContent.linkedin
            ),
            reddit = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.reddit,
                otherItemFieldValue = otherItemContents.contactDetailsContent.reddit
            ),
            facebook = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.facebook,
                otherItemFieldValue = otherItemContents.contactDetailsContent.facebook
            ),
            yahoo = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.yahoo,
                otherItemFieldValue = otherItemContents.contactDetailsContent.yahoo
            ),
            instagram = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.contactDetailsContent.instagram,
                otherItemFieldValue = otherItemContents.contactDetailsContent.instagram
            ),
            company = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.workDetailsContent.company,
                otherItemFieldValue = otherItemContents.workDetailsContent.company
            ),
            jobTitle = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.workDetailsContent.jobTitle,
                otherItemFieldValue = otherItemContents.workDetailsContent.jobTitle
            ),
            personalWebsite = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.workDetailsContent.personalWebsite,
                otherItemFieldValue = otherItemContents.workDetailsContent.personalWebsite
            ),
            workPhoneNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.workDetailsContent.workPhoneNumber,
                otherItemFieldValue = otherItemContents.workDetailsContent.workPhoneNumber
            ),
            workEmail = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.workDetailsContent.workEmail,
                otherItemFieldValue = otherItemContents.workDetailsContent.workEmail
            ),
            addressCustomFields = calculateItemDiffTypes(
                encryptionContext = this@withEncryptionContext,
                baseItemCustomFieldsContent = baseItemContents.addressDetailsContent.customFields,
                otherItemCustomFieldsContent = otherItemContents.addressDetailsContent.customFields
            ),
            contactCustomFields = calculateItemDiffTypes(
                encryptionContext = this@withEncryptionContext,
                baseItemCustomFieldsContent = baseItemContents.contactDetailsContent.customFields,
                otherItemCustomFieldsContent = otherItemContents.contactDetailsContent.customFields
            ),
            personalCustomFields = calculateItemDiffTypes(
                encryptionContext = this@withEncryptionContext,
                baseItemCustomFieldsContent = baseItemContents.personalDetailsContent.customFields,
                otherItemCustomFieldsContent = otherItemContents.personalDetailsContent.customFields
            ),
            workCustomFields = calculateItemDiffTypes(
                encryptionContext = this@withEncryptionContext,
                baseItemCustomFieldsContent = baseItemContents.workDetailsContent.customFields,
                otherItemCustomFieldsContent = otherItemContents.workDetailsContent.customFields
            ),
            extraCustomFields = baseItemContents.extraSectionContentList.mapIndexed { index, extraSectionContent ->
                calculateItemDiffTypes(
                    encryptionContext = this@withEncryptionContext,
                    baseItemCustomFieldsContent = extraSectionContent.customFieldList,
                    otherItemCustomFieldsContent = otherItemContents.extraSectionContentList
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
        fieldType: ItemDetailsFieldType.IdentityItemAction,
        callback: suspend (DetailEvent) -> Unit
    ) = Unit

    private fun observeGenericTotps(
        item: Item,
        extractTotps: suspend (ItemContents.Identity) -> Map<Pair<Option<Int>, Int>, String>
    ): Flow<Map<Pair<Option<Int>, Int>, TotpState>> = combine(
        observeItemContents(item),
        canDisplayTotp(shareId = item.shareId, itemId = item.id)
    ) { contents, canDisplayTotp -> contents to canDisplayTotp }
        .flatMapLatest { (contents, canDisplayTotp) ->
            if (!canDisplayTotp) return@flatMapLatest flowOf(emptyMap())

            val decrypted = extractTotps(contents)

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
        .onStart { emit(emptyMap()) }

    private fun observeTotps(item: Item): Flow<Map<Pair<Option<Int>, Int>, TotpState>> =
        observeGenericTotps(item) { contents ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                val sectionCustomFields = contents.extraSectionContentList
                    .flatMapIndexed { sectionIndex, sectionContent ->
                        sectionContent.customFieldList.mapToDecryptedTotp(
                            sectionIndex = sectionIndex.some(),
                            decrypt = ::decrypt
                        )
                    }
                    .toMap()

                val customFields = contents.customFields
                    .mapToDecryptedTotp(
                        sectionIndex = None,
                        decrypt = ::decrypt
                    )
                    .toMap()

                sectionCustomFields + customFields
            }
        }

    private fun observePersonalDetailTotps(item: Item): Flow<Map<Pair<Option<Int>, Int>, TotpState>> =
        observeGenericTotps(item) { contents ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                contents.personalDetailsContent.customFields
                    .mapToDecryptedTotp(
                        sectionIndex = None,
                        decrypt = ::decrypt
                    )
                    .toMap()
            }
        }

    private fun observeAddressDetailTotps(item: Item): Flow<Map<Pair<Option<Int>, Int>, TotpState>> =
        observeGenericTotps(item) { contents ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                contents.addressDetailsContent.customFields
                    .mapToDecryptedTotp(
                        sectionIndex = None,
                        decrypt = ::decrypt
                    )
                    .toMap()
            }
        }

    private fun observeWorkDetailTotps(item: Item): Flow<Map<Pair<Option<Int>, Int>, TotpState>> =
        observeGenericTotps(item) { contents ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                contents.workDetailsContent.customFields
                    .mapToDecryptedTotp(
                        sectionIndex = None,
                        decrypt = ::decrypt
                    )
                    .toMap()
            }
        }

    private fun observeContactDetailTotps(item: Item): Flow<Map<Pair<Option<Int>, Int>, TotpState>> =
        observeGenericTotps(item) { contents ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                contents.contactDetailsContent.customFields
                    .mapToDecryptedTotp(
                        sectionIndex = None,
                        decrypt = ::decrypt
                    )
                    .toMap()
            }
        }
}
