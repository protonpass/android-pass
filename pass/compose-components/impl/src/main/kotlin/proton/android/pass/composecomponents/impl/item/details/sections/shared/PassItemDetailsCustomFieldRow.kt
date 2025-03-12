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
import kotlinx.collections.immutable.ImmutableMap
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.presentation.R
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonuimodels.api.masks.TextMask
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailMaskedFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.progress.PassTotpProgress
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.Totp

private const val HIDDEN_CUSTOM_FIELD_TEXT_LENGTH = 12

@Composable
internal fun PassItemDetailsCustomFieldRow(
    modifier: Modifier = Modifier,
    customFieldIndex: Int,
    customFieldContent: CustomFieldContent,
    customFieldSection: ItemSection,
    customFieldTotps: ImmutableMap<Pair<Option<Int>, Int>, Totp>,
    itemColors: PassItemColors,
    itemDiffType: ItemDiffType,
    onEvent: (PassItemDetailsUiEvent) -> Unit,
    @DrawableRes iconResId: Int? = null
) {
    when (customFieldContent) {
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
                    PassItemDetailsUiEvent.OnHiddenFieldClick(
                        state = customFieldContent.value,
                        field = ItemDetailsFieldType.Hidden.CustomField(customFieldIndex)
                    )
                )
            },
            onToggle = { isVisible ->
                onEvent(
                    PassItemDetailsUiEvent.OnHiddenFieldToggle(
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
            val sectionIndex = (customFieldSection as? ItemSection.ExtraSection)
                ?.index
                .toOption()
            customFieldTotps[sectionIndex to customFieldIndex]?.let { customFieldTotp ->
                PassItemDetailMaskedFieldRow(
                    icon = R.drawable.ic_proton_lock,
                    title = customFieldContent.label,
                    maskedSubtitle = TextMask.TotpCode(customFieldTotp.code),
                    itemColors = itemColors,
                    itemDiffType = itemDiffType,
                    onClick = {
                        onEvent(
                            PassItemDetailsUiEvent.OnSectionClick(
                                section = customFieldTotp.code,
                                field = ItemDetailsFieldType.Plain.TotpCode
                            )
                        )
                    },
                    contentInBetween = {
                        PassTotpProgress(
                            remainingSeconds = customFieldTotp.remainingSeconds,
                            totalSeconds = customFieldTotp.totalSeconds
                        )
                    }
                )
            }
        }

        is CustomFieldContent.Date -> {
            // Needs to be implemented
        }
    }
}

@Suppress("LongParameterList")
internal fun MutableList<@Composable () -> Unit>.addCustomFieldRows(
    customFields: List<CustomFieldContent>,
    customFieldSection: ItemSection,
    customFieldTotps: ImmutableMap<Pair<Option<Int>, Int>, Totp>,
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
                customFieldTotps = customFieldTotps,
                itemColors = itemColors,
                itemDiffType = itemDiffType,
                onEvent = onEvent
            )
        }
    }
}
