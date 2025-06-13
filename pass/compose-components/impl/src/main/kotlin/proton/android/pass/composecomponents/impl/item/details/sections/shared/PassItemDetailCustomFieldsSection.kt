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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.DateFormatUtils
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailTOTPFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.TotpState
import me.proton.core.presentation.R as CoreR

private const val HIDDEN_CUSTOM_FIELD_TEXT_LENGTH = 12

@Composable
internal fun PassItemDetailCustomFieldsSection(
    modifier: Modifier = Modifier,
    customFields: ImmutableList<CustomFieldContent>,
    customFieldTotps: ImmutableMap<Pair<Option<Int>, Int>, TotpState>,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        customFields.forEachIndexed { index, customFieldContent ->
            RoundedCornersColumn {
                when (customFieldContent) {
                    is CustomFieldContent.Text -> PassItemDetailFieldRow(
                        icon = CoreR.drawable.ic_proton_text_align_left,
                        title = customFieldContent.label,
                        subtitle = customFieldContent.value,
                        itemColors = itemColors,
                        itemDiffType = itemDiffs.customField(index),
                        onClick = {
                            onEvent(
                                PassItemDetailsUiEvent.OnFieldClick(
                                    field = ItemDetailsFieldType.PlainCopyable.CustomField(
                                        text = customFieldContent.value
                                    )
                                )
                            )
                        }
                    )

                    is CustomFieldContent.Hidden -> PassItemDetailsHiddenFieldRow(
                        icon = CoreR.drawable.ic_proton_eye_slash,
                        title = customFieldContent.label,
                        hiddenState = customFieldContent.value,
                        hiddenTextLength = HIDDEN_CUSTOM_FIELD_TEXT_LENGTH,
                        itemColors = itemColors,
                        itemDiffType = itemDiffs.customField(index),
                        hiddenTextStyle = ProtonTheme.typography.defaultNorm,
                        onClick = {
                            onEvent(
                                PassItemDetailsUiEvent.OnFieldClick(
                                    field = ItemDetailsFieldType.HiddenCopyable.CustomField(
                                        hiddenState = customFieldContent.value,
                                        index = index
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
                                        index = index
                                    ),
                                    fieldSection = ItemSection.CustomField
                                )
                            )
                        }
                    )

                    is CustomFieldContent.Totp -> {
                        customFieldTotps[None to index]?.let { customFieldTotp ->
                            PassItemDetailTOTPFieldRow(
                                totp = customFieldTotp,
                                icon = CoreR.drawable.ic_proton_lock,
                                title = customFieldContent.label,
                                itemColors = itemColors,
                                itemDiffType = itemDiffs.customField(index),
                                onEvent = onEvent
                            )
                        }
                    }

                    is CustomFieldContent.Date -> {
                        val pattern = stringResource(R.string.custom_field_date_format)
                        val date: String = remember(pattern, customFieldContent.value) {
                            customFieldContent.value.value()?.let {
                                DateFormatUtils.formatDateFromMillis(pattern, it)
                            }.orEmpty()
                        }
                        PassItemDetailFieldRow(
                            modifier = modifier,
                            icon = CoreR.drawable.ic_proton_calendar_today,
                            title = customFieldContent.label,
                            subtitle = date,
                            itemColors = itemColors,
                            itemDiffType = itemDiffs.customField(index)
                        )
                    }
                }
            }
        }
    }
}
