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
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.masks.TextMask
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailMaskedFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.progress.PassTotpProgress
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemCustomFieldSection
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.Totp
import me.proton.core.presentation.R as CoreR

private const val HIDDEN_CUSTOM_FIELD_TEXT_LENGTH = 12

@Composable
internal fun PassItemDetailCustomFieldsSection(
    modifier: Modifier = Modifier,
    customFields: ImmutableList<CustomFieldContent>,
    secondaryTotps: ImmutableMap<String, Totp?>,
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
                                PassItemDetailsUiEvent.OnSectionClick(
                                    section = customFieldContent.value,
                                    field = ItemDetailsFieldType.Plain.CustomField
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
                                PassItemDetailsUiEvent.OnHiddenSectionClick(
                                    state = customFieldContent.value,
                                    field = ItemDetailsFieldType.Hidden.CustomField(index)
                                )
                            )
                        },
                        onToggle = { isVisible ->
                            onEvent(
                                PassItemDetailsUiEvent.OnHiddenSectionToggle(
                                    isVisible = isVisible,
                                    hiddenState = customFieldContent.value,
                                    fieldType = ItemDetailsFieldType.Hidden.CustomField(index),
                                    fieldSection = ItemCustomFieldSection.CustomField
                                )
                            )
                        }
                    )

                    is CustomFieldContent.Totp -> {
                        secondaryTotps[customFieldContent.label]?.let { customFieldTotp ->
                            PassItemDetailMaskedFieldRow(
                                icon = CoreR.drawable.ic_proton_lock,
                                title = customFieldContent.label,
                                maskedSubtitle = TextMask.TotpCode(customFieldTotp.code),
                                itemColors = itemColors,
                                itemDiffType = itemDiffs.customField(index),
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
                }
            }
        }
    }
}
