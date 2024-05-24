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
            is ItemContents.Unknown -> return false
        }
    }

    private fun isAliasMatch(content: ItemContents.Alias, query: String): Boolean =
        content.aliasEmail.preprocess().contains(query)

    private fun isIdentityMatch(content: ItemContents.Identity, query: String): Boolean {
        val identityProperties = listOf(
            content.personalDetails.fullName,
            content.personalDetails.firstName,
            content.personalDetails.middleName,
            content.personalDetails.lastName,
            content.personalDetails.birthdate,
            content.personalDetails.gender,
            content.personalDetails.email,
            content.personalDetails.phoneNumber,
            content.addressDetails.organization,
            content.addressDetails.streetAddress,
            content.addressDetails.zipOrPostalCode,
            content.addressDetails.city,
            content.addressDetails.stateOrProvince,
            content.addressDetails.countryOrRegion,
            content.addressDetails.floor,
            content.addressDetails.county,
            content.contactDetails.socialSecurityNumber,
            content.contactDetails.passportNumber,
            content.contactDetails.licenseNumber,
            content.contactDetails.website,
            content.contactDetails.xHandle,
            content.contactDetails.secondPhoneNumber,
            content.contactDetails.linkedin,
            content.contactDetails.reddit,
            content.contactDetails.facebook,
            content.contactDetails.yahoo,
            content.contactDetails.instagram,
            content.workDetails.company,
            content.workDetails.jobTitle,
            content.workDetails.personalWebsite,
            content.workDetails.workPhoneNumber,
            content.workDetails.workEmail
        )
        identityProperties.forEach { fieldValue ->
            if (fieldValue.preprocess().contains(query)) {
                return true
            }
        }

        return false
    }

    @Suppress("ReturnCount")
    private fun isLoginMatch(content: ItemContents.Login, query: String): Boolean {
        if (content.itemEmail.preprocess().contains(query)) return true

        val anyWebsiteMatches = content.urls.any { it.preprocess().contains(query) }
        if (anyWebsiteMatches) return true

        val textCustomFields: List<CustomFieldContent.Text> = content.customFields.filterByType()

        val anyCustomFieldLabelMatches = textCustomFields.any { it.label.preprocess().contains(query) }
        if (anyCustomFieldLabelMatches) return true

        val anyCustomFieldValueMatches = textCustomFields.any { it.value.preprocess().contains(query) }
        if (anyCustomFieldValueMatches) return true

        return false
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
