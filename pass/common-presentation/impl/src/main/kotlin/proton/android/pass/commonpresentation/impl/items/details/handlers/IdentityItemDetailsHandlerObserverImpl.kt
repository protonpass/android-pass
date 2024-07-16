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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldSection
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemState
import javax.inject.Inject

class IdentityItemDetailsHandlerObserverImpl @Inject constructor(
    private val observeVaultById: GetVaultById,
    private val encryptionContextProvider: EncryptionContextProvider
) : ItemDetailsHandlerObserver<ItemContents.Identity>() {

    override fun observe(item: Item): Flow<ItemDetailState> = combine(
        observeIdentityItemContents(item),
        observeVaultById(shareId = item.shareId)
    ) { identityItemContents, vault ->
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
            itemVault = vault
        )
    }

    private fun observeIdentityItemContents(item: Item): Flow<ItemContents.Identity> = flow {
        encryptionContextProvider.withEncryptionContext {
            item.toItemContents(this@withEncryptionContext) as ItemContents.Identity
        }.let { identityItemContents ->
            emit(identityItemContents)
        }
    }

    @Suppress("LongMethod")
    override fun updateItemContents(
        itemContents: ItemContents.Identity,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        hiddenFieldSection: ItemDetailsFieldSection,
        hiddenState: HiddenState
    ): ItemContents = when (hiddenFieldType) {
        is ItemDetailsFieldType.Hidden.CustomField -> {
            when (hiddenFieldSection) {
                ItemDetailsFieldSection.Identity.Address -> {
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

                ItemDetailsFieldSection.Identity.Contact -> {
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

                is ItemDetailsFieldSection.Identity.ExtraSection -> {
                    itemContents.copy(
                        extraSectionContentList = itemContents.extraSectionContentList
                            .toMutableList()
                            .apply {
                                itemContents.extraSectionContentList[hiddenFieldSection.index]
                                    .let { extraSectionContent ->
                                        set(
                                            index = hiddenFieldSection.index,
                                            element = extraSectionContent.copy(
                                                customFields = toggleHiddenCustomField(
                                                    customFieldsContent = extraSectionContent.customFields,
                                                    hiddenFieldType = hiddenFieldType,
                                                    hiddenState = hiddenState
                                                )
                                            )
                                        )
                                    }
                            }
                    )
                }

                ItemDetailsFieldSection.Identity.Personal -> {
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

                ItemDetailsFieldSection.Identity.Work -> {
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

                ItemDetailsFieldSection.Main -> itemContents
            }
        }

        ItemDetailsFieldType.Hidden.Cvv,
        ItemDetailsFieldType.Hidden.Password,
        ItemDetailsFieldType.Hidden.Pin -> itemContents
    }

    @Suppress("LongMethod")
    override fun calculateItemDiffs(
        baseItemDetailState: ItemContents.Identity,
        otherItemDetailState: ItemContents.Identity
    ): ItemDiffs = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.Identity(
            title = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.title,
                otherItemFieldValue = otherItemDetailState.title
            ),
            note = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.note,
                otherItemFieldValue = otherItemDetailState.note
            ),
            fullName = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.personalDetailsContent.fullName,
                otherItemFieldValue = otherItemDetailState.personalDetailsContent.fullName
            ),
            firstName = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.personalDetailsContent.firstName,
                otherItemFieldValue = otherItemDetailState.personalDetailsContent.firstName
            ),
            middleName = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.personalDetailsContent.middleName,
                otherItemFieldValue = otherItemDetailState.personalDetailsContent.middleName
            ),
            lastName = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.personalDetailsContent.lastName,
                otherItemFieldValue = otherItemDetailState.personalDetailsContent.lastName
            ),
            birthdate = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.personalDetailsContent.birthdate,
                otherItemFieldValue = otherItemDetailState.personalDetailsContent.birthdate
            ),
            gender = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.personalDetailsContent.gender,
                otherItemFieldValue = otherItemDetailState.personalDetailsContent.gender
            ),
            email = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.personalDetailsContent.email,
                otherItemFieldValue = otherItemDetailState.personalDetailsContent.email
            ),
            phoneNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.personalDetailsContent.phoneNumber,
                otherItemFieldValue = otherItemDetailState.personalDetailsContent.phoneNumber
            ),
            organization = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.addressDetailsContent.organization,
                otherItemFieldValue = otherItemDetailState.addressDetailsContent.organization
            ),
            streetAddress = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.addressDetailsContent.streetAddress,
                otherItemFieldValue = otherItemDetailState.addressDetailsContent.streetAddress
            ),
            zipOrPostalCode = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.addressDetailsContent.zipOrPostalCode,
                otherItemFieldValue = otherItemDetailState.addressDetailsContent.zipOrPostalCode
            ),
            city = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.addressDetailsContent.city,
                otherItemFieldValue = otherItemDetailState.addressDetailsContent.city
            ),
            stateOrProvince = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.addressDetailsContent.stateOrProvince,
                otherItemFieldValue = otherItemDetailState.addressDetailsContent.stateOrProvince
            ),
            countryOrRegion = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.addressDetailsContent.countryOrRegion,
                otherItemFieldValue = otherItemDetailState.addressDetailsContent.countryOrRegion
            ),
            floor = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.addressDetailsContent.floor,
                otherItemFieldValue = otherItemDetailState.addressDetailsContent.floor
            ),
            county = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.addressDetailsContent.county,
                otherItemFieldValue = otherItemDetailState.addressDetailsContent.county
            ),
            socialSecurityNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.socialSecurityNumber,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.socialSecurityNumber
            ),
            passportNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.passportNumber,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.passportNumber
            ),
            licenseNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.licenseNumber,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.licenseNumber
            ),
            website = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.website,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.website
            ),
            xHandle = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.xHandle,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.xHandle
            ),
            secondPhoneNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.secondPhoneNumber,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.secondPhoneNumber
            ),
            linkedin = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.linkedin,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.linkedin
            ),
            reddit = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.reddit,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.reddit
            ),
            facebook = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.facebook,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.facebook
            ),
            yahoo = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.yahoo,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.yahoo
            ),
            instagram = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.contactDetailsContent.instagram,
                otherItemFieldValue = otherItemDetailState.contactDetailsContent.instagram
            ),
            company = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.workDetailsContent.company,
                otherItemFieldValue = otherItemDetailState.workDetailsContent.company
            ),
            jobTitle = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.workDetailsContent.jobTitle,
                otherItemFieldValue = otherItemDetailState.workDetailsContent.jobTitle
            ),
            personalWebsite = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.workDetailsContent.personalWebsite,
                otherItemFieldValue = otherItemDetailState.workDetailsContent.personalWebsite
            ),
            workPhoneNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.workDetailsContent.workPhoneNumber,
                otherItemFieldValue = otherItemDetailState.workDetailsContent.workPhoneNumber
            ),
            workEmail = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.workDetailsContent.workEmail,
                otherItemFieldValue = otherItemDetailState.workDetailsContent.workEmail
            )
        )
    }

}
