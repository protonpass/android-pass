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

package proton.android.pass.composecomponents.impl.item.details.sections.shared

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemCustomFieldSection
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs

private const val HIDDEN_CUSTOM_FIELD_TEXT_LENGTH = 12

@Composable
internal fun PassItemDetailsCustomFieldRow(
    modifier: Modifier = Modifier,
    customFieldIndex: Int,
    customFieldContent: CustomFieldContent,
    customFieldSection: ItemCustomFieldSection,
    itemColors: PassItemColors,
    itemDiffType: ItemDiffType,
    onEvent: (PassItemDetailsUiEvent) -> Unit,
    @DrawableRes iconResId: Int? = null
) = when (customFieldContent) {
    is CustomFieldContent.Hidden -> PassItemDetailsHiddenFieldRow(
        icon = null,
        title = customFieldContent.label,
        hiddenState = customFieldContent.value,
        hiddenTextLength = HIDDEN_CUSTOM_FIELD_TEXT_LENGTH,
        itemColors = itemColors,
        itemDiffType = itemDiffType,
        hiddenTextStyle = ProtonTheme.typography.defaultNorm,
        onClick = {
            onEvent(
                PassItemDetailsUiEvent.OnHiddenSectionClick(
                    state = customFieldContent.value,
                    field = ItemDetailsFieldType.Hidden.CustomField(customFieldIndex)
                )
            )
        },
        onToggle = { isVisible ->
            onEvent(
                PassItemDetailsUiEvent.OnHiddenSectionToggle(
                    isVisible = isVisible,
                    hiddenState = customFieldContent.value,
                    fieldType = ItemDetailsFieldType.Hidden.CustomField(customFieldIndex),
                    fieldSection = customFieldSection
                )
            )
        }
    )

    is CustomFieldContent.Text -> PassItemDetailFieldRow(
        modifier = modifier,
        icon = iconResId,
        title = customFieldContent.label,
        subtitle = customFieldContent.value,
        itemColors = itemColors,
        itemDiffType = itemDiffType,
        onClick = {
            onEvent(
                PassItemDetailsUiEvent.OnSectionClick(
                    section = customFieldContent.value,
                    field = ItemDetailsFieldType.Plain.CustomField
                )
            )
        }
    )

    is CustomFieldContent.Totp -> {
        // Needs to be implemented
    }

    is CustomFieldContent.Date -> {
        // Needs to be implemented
    }
}

internal fun MutableList<@Composable () -> Unit>.addCustomFieldRows(
    customFields: List<CustomFieldContent>,
    customFieldSection: ItemCustomFieldSection,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    customFields.forEachIndexed { index, customFieldContent ->
        add {
            val itemDiffType = when (itemDiffs) {
                is ItemDiffs.Identity -> itemDiffs.customField(customFieldSection, index)
                is ItemDiffs.WifiNetwork -> itemDiffs.customField(customFieldSection, index)
                is ItemDiffs.SSHKey -> itemDiffs.customField(customFieldSection, index)
                is ItemDiffs.Custom -> itemDiffs.customField(customFieldSection, index)
                is ItemDiffs.Login,
                ItemDiffs.None,
                is ItemDiffs.Note,
                is ItemDiffs.Alias,
                is ItemDiffs.CreditCard,
                is ItemDiffs.Unknown ->
                    throw UnsupportedOperationException("sections in ${itemDiffs::class.simpleName} ")
            }
            PassItemDetailsCustomFieldRow(
                customFieldIndex = index,
                customFieldContent = customFieldContent,
                customFieldSection = customFieldSection,
                itemColors = itemColors,
                itemDiffType = itemDiffType,
                onEvent = onEvent
            )
        }
    }
}
