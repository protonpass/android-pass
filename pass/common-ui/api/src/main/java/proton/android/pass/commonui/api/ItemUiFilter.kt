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
            content.personalDetailsContent.fullName,
            content.personalDetailsContent.firstName,
            content.personalDetailsContent.middleName,
            content.personalDetailsContent.lastName,
            content.personalDetailsContent.birthdate,
            content.personalDetailsContent.gender,
            content.personalDetailsContent.email,
            content.personalDetailsContent.phoneNumber,
            content.addressDetailsContent.organization,
            content.addressDetailsContent.streetAddress,
            content.addressDetailsContent.zipOrPostalCode,
            content.addressDetailsContent.city,
            content.addressDetailsContent.stateOrProvince,
            content.addressDetailsContent.countryOrRegion,
            content.addressDetailsContent.floor,
            content.addressDetailsContent.county,
            content.contactDetailsContent.socialSecurityNumber,
            content.contactDetailsContent.passportNumber,
            content.contactDetailsContent.licenseNumber,
            content.contactDetailsContent.website,
            content.contactDetailsContent.xHandle,
            content.contactDetailsContent.secondPhoneNumber,
            content.contactDetailsContent.linkedin,
            content.contactDetailsContent.reddit,
            content.contactDetailsContent.facebook,
            content.contactDetailsContent.yahoo,
            content.contactDetailsContent.instagram,
            content.workDetailsContent.company,
            content.workDetailsContent.jobTitle,
            content.workDetailsContent.personalWebsite,
            content.workDetailsContent.workPhoneNumber,
            content.workDetailsContent.workEmail
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

        if (content.itemUsername.preprocess().contains(query)) return true

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
