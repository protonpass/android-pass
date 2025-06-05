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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableMap
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.DateFormatUtils
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailTOTPFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.TotpState

private const val HIDDEN_CUSTOM_FIELD_TEXT_LENGTH = 12

@Composable
internal fun PassItemDetailsCustomFieldRow(
    modifier: Modifier = Modifier,
    customFieldIndex: Int,
    customFieldContent: CustomFieldContent,
    customFieldSection: ItemSection,
    customFieldTotps: ImmutableMap<Pair<Option<Int>, Int>, TotpState>,
    itemColors: PassItemColors,
    itemDiffType: ItemDiffType,
    onEvent: (PassItemDetailsUiEvent) -> Unit
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
                    PassItemDetailsUiEvent.OnFieldClick(
                        field = ItemDetailsFieldType.HiddenCopyable.CustomField(
                            hiddenState = customFieldContent.value,
                            index = customFieldIndex
                        )
                    )
                )
            },
            onToggle = { isVisible ->
                onEvent(
                    PassItemDetailsUiEvent.OnHiddenFieldToggle(
                        isVisible = isVisible,
                        hiddenState = customFieldContent.value,
                        fieldType = ItemDetailsFieldType.HiddenCopyable.CustomField(
                            hiddenState = customFieldContent.value,
                            index = customFieldIndex
                        ),
                        fieldSection = customFieldSection
                    )
                )
            }
        )

        is CustomFieldContent.Text -> PassItemDetailFieldRow(
            modifier = modifier,
            icon = null,
            title = customFieldContent.label,
            subtitle = customFieldContent.value,
            itemColors = itemColors,
            itemDiffType = itemDiffType,
            onClick = {
                onEvent(
                    PassItemDetailsUiEvent.OnFieldClick(
                        field = ItemDetailsFieldType.PlainCopyable.CustomField(customFieldContent.value)
                    )
                )
            }
        )

        is CustomFieldContent.Totp -> {
            val sectionIndex = (customFieldSection as? ItemSection.ExtraSection)
                ?.index
                .toOption()
            customFieldTotps[sectionIndex to customFieldIndex]?.let { customFieldTotp ->
                PassItemDetailTOTPFieldRow(
                    totp = customFieldTotp,
                    title = customFieldContent.label,
                    itemColors = itemColors,
                    itemDiffType = itemDiffType,
                    onEvent = onEvent
                )
            }
        }

        is CustomFieldContent.Date -> {
            val pattern = stringResource(R.string.custom_field_date_format)
            val date = remember(pattern, customFieldContent.value) {
                customFieldContent.value.value()?.let {
                    DateFormatUtils.formatDateFromMillis(pattern, it)
                }
            }.orEmpty()
            PassItemDetailFieldRow(
                modifier = modifier,
                icon = null,
                title = customFieldContent.label,
                subtitle = date,
                itemColors = itemColors,
                itemDiffType = itemDiffType
            )
        }
    }
}

@Suppress("LongParameterList")
internal fun MutableList<@Composable () -> Unit>.addCustomFieldRows(
    customFields: List<CustomFieldContent>,
    customFieldSection: ItemSection,
    customFieldTotps: ImmutableMap<Pair<Option<Int>, Int>, TotpState>,
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
