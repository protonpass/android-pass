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

package proton.android.pass.commonui.api

import proton.android.pass.common.api.filterByType
import proton.android.pass.common.api.removeAccents
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemContents

object ItemUiFilter {

    fun List<ItemUiModel>.filterByQuery(query: String): List<ItemUiModel> = if (query.isNotEmpty()) {
        if (query.isNotBlank()) {
            val cleanQuery = query.preprocess()
            filter { it.matchesQuery(cleanQuery) }
        } else {
            emptyList()
        }
    } else {
        this
    }

    private fun isItemMatch(item: ItemUiModel, query: String): Boolean {
        if (item.contents.title.preprocess().contains(query)) return true
        if (item.contents.note.preprocess().contains(query)) return true

        return when (val contents = item.contents) {
            is ItemContents.Alias -> isAliasMatch(contents, query)
            is ItemContents.Login -> isLoginMatch(contents, query)
            is ItemContents.Note -> isNoteMatch(contents, query)
            is ItemContents.CreditCard -> isCreditCardMatch(contents, query)
            is ItemContents.Identity -> isIdentityMatch(contents, query)
            is ItemContents.Custom -> isCustomMatch(contents, query)
            is ItemContents.Unknown -> return false
        }
    }

    private fun isAliasMatch(content: ItemContents.Alias, query: String): Boolean =
        content.aliasEmail.preprocess().contains(query)

    private fun isIdentityMatch(content: ItemContents.Identity, query: String): Boolean {
        val personalDetails = content.personalDetailsContent
        val addressDetails = content.addressDetailsContent
        val contactDetails = content.contactDetailsContent
        val workDetails = content.workDetailsContent

        val identityProperties = listOf(
            personalDetails.fullName,
            personalDetails.firstName,
            personalDetails.middleName,
            personalDetails.lastName,
            personalDetails.birthdate,
            personalDetails.gender,
            personalDetails.email,
            personalDetails.phoneNumber,
            addressDetails.organization,
            addressDetails.streetAddress,
            addressDetails.zipOrPostalCode,
            addressDetails.city,
            addressDetails.stateOrProvince,
            addressDetails.countryOrRegion,
            addressDetails.floor,
            addressDetails.county,
            contactDetails.socialSecurityNumber,
            contactDetails.passportNumber,
            contactDetails.licenseNumber,
            contactDetails.website,
            contactDetails.xHandle,
            contactDetails.secondPhoneNumber,
            contactDetails.linkedin,
            contactDetails.reddit,
            contactDetails.facebook,
            contactDetails.yahoo,
            contactDetails.instagram,
            workDetails.company,
            workDetails.jobTitle,
            workDetails.personalWebsite,
            workDetails.workPhoneNumber,
            workDetails.workEmail
        )
        identityProperties.forEach { fieldValue ->
            if (fieldValue.preprocess().contains(query)) {
                return true
            }
        }
        return when {
            hasMatchingCustomField(personalDetails.customFields, query) -> true
            hasMatchingCustomField(addressDetails.customFields, query) -> true
            hasMatchingCustomField(contactDetails.customFields, query) -> true
            hasMatchingCustomField(workDetails.customFields, query) -> true
            else -> {
                content.extraSectionContentList.forEach {
                    if (hasMatchingCustomField(it.customFields, query)) return true
                }

                false
            }
        }
    }

    private fun isCustomMatch(content: ItemContents.Custom, query: String): Boolean = when {
        else -> {
            content.sectionContentList.forEach {
                if (hasMatchingCustomField(it.customFields, query)) return true
            }

            false
        }
    }

    private fun hasMatchingCustomField(customFields: List<CustomFieldContent>, query: String): Boolean {
        val customFieldsText: List<CustomFieldContent.Text> = customFields.filterByType()
        return customFieldsText.any { it.label.preprocess().contains(query) || it.value.preprocess().contains(query) }
    }

    private fun isLoginMatch(content: ItemContents.Login, query: String): Boolean {
        if (content.itemEmail.preprocess().contains(query)) return true

        if (content.itemUsername.preprocess().contains(query)) return true

        val anyWebsiteMatches = content.urls.any { it.preprocess().contains(query) }
        if (anyWebsiteMatches) return true

        val textCustomFields: List<CustomFieldContent.Text> = content.customFields.filterByType()
        return hasMatchingCustomField(textCustomFields, query)
    }

    private fun isNoteMatch(content: ItemContents.Note, query: String): Boolean =
        content.note.preprocess().contains(query)

    private fun isCreditCardMatch(content: ItemContents.CreditCard, query: String): Boolean {
        if (content.title.preprocess().contains(query)) return true
        if (content.cardHolder.preprocess().contains(query)) return true
        if (content.note.preprocess().contains(query)) return true

        return false
    }

    private fun ItemUiModel.matchesQuery(query: String): Boolean {
        val queryParts = query.split(" ").filter { it.isNotBlank() }
        return queryParts.all { isItemMatch(this, it) }
    }

    private fun String.preprocess(): String = this.lowercase().removeAccents()

}
