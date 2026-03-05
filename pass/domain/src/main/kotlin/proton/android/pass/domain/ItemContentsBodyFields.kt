/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.domain

/**
 * Returns all body-text strings that are both matchable by the search filter and displayable
 * as highlighted subtitles in item rows when they match.
 *
 * This is the single source of truth shared by ItemUiFilter and each *Row composable.
 * The exhaustive when expression ensures the compiler breaks if a new ItemContents
 * subtype is added without being handled here.
 */
fun ItemContents.highlightableBodyFields(): List<String> {
    val common = customFields.toHighlightableStrings()
    val typeSpecific: List<String> = when (this) {
        is ItemContents.Identity -> buildList {
            personalDetailsContent.customFields.mapNotNullTo(this) { it.toDisplayString() }
            addressDetailsContent.customFields.mapNotNullTo(this) { it.toDisplayString() }
            contactDetailsContent.customFields.mapNotNullTo(this) { it.toDisplayString() }
            workDetailsContent.customFields.mapNotNullTo(this) { it.toDisplayString() }
            extraSectionContentList.forEach { addAll(it.toHighlightableStrings()) }
        }
        is ItemContents.Custom -> sectionContentList.flatMap { it.toHighlightableStrings() }
        is ItemContents.WifiNetwork -> buildList {
            if (ssid.isNotBlank()) add(ssid)
            sectionContentList.forEach { addAll(it.toHighlightableStrings()) }
        }
        is ItemContents.SSHKey -> sectionContentList.flatMap { it.toHighlightableStrings() }
        is ItemContents.Login,
        is ItemContents.Note,
        is ItemContents.Alias,
        is ItemContents.CreditCard,
        is ItemContents.Unknown -> emptyList()
    }
    return common + typeSpecific
}

fun ExtraSectionContent.toHighlightableStrings(): List<String> = buildList {
    if (title.isNotBlank()) add(title)
    customFieldList.mapNotNullTo(this) { it.toDisplayString() }
}

fun List<CustomFieldContent>.toHighlightableStrings(): List<String> = mapNotNull { it.toDisplayString() }

fun CustomFieldContent.toDisplayString(): String? = when (this) {
    is CustomFieldContent.Text -> "$label: $value".takeIf { label.isNotBlank() || value.isNotBlank() }
    is CustomFieldContent.Hidden -> label.takeIf { it.isNotBlank() }
    is CustomFieldContent.Totp -> label.takeIf { it.isNotBlank() }
    is CustomFieldContent.Date -> label.takeIf { it.isNotBlank() }
}
