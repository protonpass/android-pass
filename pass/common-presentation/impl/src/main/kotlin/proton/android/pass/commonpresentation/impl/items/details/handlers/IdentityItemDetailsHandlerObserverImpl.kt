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

class IdentityItemDetailsHandlerObserverImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : ItemDetailsHandlerObserver<ItemContents.Identity>() {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState
    ): Flow<ItemDetailState> = observeIdentityItemContents(item)
        .mapLatest { identityItemContents ->
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
                attachmentsState = attachmentsState
            )
        }

    private fun observeIdentityItemContents(item: Item): Flow<ItemContents.Identity> = flow {
        encryptionContextProvider.withEncryptionContext {
            toItemContents(
                itemType = item.itemType,
                encryptionContext = this,
                title = item.title,
                note = item.note,
                flags = item.flags
            ) as ItemContents.Identity
        }.let { identityItemContents ->
            emit(identityItemContents)
        }
    }

    @Suppress("LongMethod")
    override fun updateItemContents(
        itemContents: ItemContents.Identity,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        hiddenFieldSection: ItemCustomFieldSection,
        hiddenState: HiddenState
    ): ItemContents = when (hiddenFieldType) {
        is ItemDetailsFieldType.Hidden.CustomField -> {
            when (hiddenFieldSection) {
                ItemCustomFieldSection.Identity.Address -> {
                    itemContents.copy(
                        addressDetailsContent = itemContents.addressDetailsContent.copy(
                            customFields = toggleHiddenCustomField(
                                customFieldsContent = itemContents.addressDetailsContent.customFields,
                                hiddenFieldType = hiddenFieldType,
                                hiddenState = hiddenState
                            )
                        )
                    )
                }

                ItemCustomFieldSection.Identity.Contact -> {
                    itemContents.copy(
                        contactDetailsContent = itemContents.contactDetailsContent.copy(
                            customFields = toggleHiddenCustomField(
                                customFieldsContent = itemContents.contactDetailsContent.customFields,
                                hiddenFieldType = hiddenFieldType,
                                hiddenState = hiddenState
                            )
                        )
                    )
                }

                is ItemCustomFieldSection.Identity.ExtraSection -> {
                    itemContents.copy(
                        extraSectionContentList = itemContents.extraSectionContentList
                            .toMutableList()
                            .apply {
                                itemContents.extraSectionContentList[hiddenFieldSection.index]
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
                }

                ItemCustomFieldSection.Identity.Personal -> {
                    itemContents.copy(
                        personalDetailsContent = itemContents.personalDetailsContent.copy(
                            customFields = toggleHiddenCustomField(
                                customFieldsContent = itemContents.personalDetailsContent.customFields,
                                hiddenFieldType = hiddenFieldType,
                                hiddenState = hiddenState
                            )
                        )
                    )
                }

                ItemCustomFieldSection.Identity.Work -> {
                    itemContents.copy(
                        workDetailsContent = itemContents.workDetailsContent.copy(
                            customFields = toggleHiddenCustomField(
                                customFieldsContent = itemContents.workDetailsContent.customFields,
                                hiddenFieldType = hiddenFieldType,
                                hiddenState = hiddenState
                            )
                        )
                    )
                }

                ItemCustomFieldSection.CustomField,
                is ItemCustomFieldSection.CustomItem.ExtraSection ->
                    throw UnsupportedOperationException(
                        "cannot have custom section in ${hiddenFieldType::class.simpleName}"
                    )
            }
        }

        ItemDetailsFieldType.Hidden.Cvv,
        ItemDetailsFieldType.Hidden.Password,
        ItemDetailsFieldType.Hidden.Pin -> itemContents
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
                baseItemFieldValue = baseItemContents.contactDetailsContent.socialSecurityNumber,
                otherItemFieldValue = otherItemContents.contactDetailsContent.socialSecurityNumber
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

}
