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

package proton.android.pass.composecomponents.impl.item.details.sections.shared

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import me.proton.core.compose.component.ProtonButton
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.PassHistoryItemRow
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passFormattedDateText
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import me.proton.core.presentation.R as CoreR

@Composable
fun PassItemDetailsHistorySection(
    modifier: Modifier = Modifier,
    lastAutofillAtOption: Option<Instant>,
    revision: Long,
    createdAt: Instant,
    modifiedAt: Instant,
    onViewItemHistoryClicked: () -> Unit,
    itemColors: PassItemColors,
    shouldDisplayItemHistoryButton: Boolean
) {
    RoundedCornersColumn(
        modifier = modifier
    ) {
        if (lastAutofillAtOption is Some) {
            PassHistoryItemRow(
                leadingIcon = CoreR.drawable.ic_proton_magic_proton_wand,
                title = stringResource(id = R.string.item_details_shared_section_item_history_last_autofill_title),
                subtitle = passFormattedDateText(endInstant = lastAutofillAtOption.value),
                paddingValues = PaddingValues(
                    top = Spacing.medium,
                    start = Spacing.medium
                )
            )
        }

        PassHistoryItemRow(
            leadingIcon = CoreR.drawable.ic_proton_pencil,
            title = stringResource(
                id = R.string.item_details_shared_section_item_history_modified_times_title,
                revision
            ),
            subtitle = passFormattedDateText(endInstant = modifiedAt),
            paddingValues = PaddingValues(
                top = Spacing.medium,
                start = Spacing.medium
            )
        )

        PassHistoryItemRow(
            leadingIcon = CoreR.drawable.ic_proton_bolt,
            title = stringResource(id = R.string.item_details_shared_section_item_history_created_title),
            subtitle = passFormattedDateText(endInstant = createdAt),
            paddingValues = PaddingValues(
                top = Spacing.medium,
                start = Spacing.medium,
                bottom = Spacing.medium
            )
        )

        if (shouldDisplayItemHistoryButton) {
            ProtonButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.medium,
                        end = Spacing.medium,
                        bottom = Spacing.medium
                    ),
                onClick = onViewItemHistoryClicked,
                shape = RoundedCornerShape(size = Radius.large),
                colors = ButtonDefaults.buttonColors(itemColors.minorSecondary),
                contentPadding = PaddingValues(
                    horizontal = Spacing.mediumSmall,
                    vertical = Spacing.mediumSmall
                ),
                elevation = null,
                border = null
            ) {
                Text(
                    text = stringResource(id = R.string.item_details_shared_section_item_history_button_view),
                    fontSize = 16.sp,
                    color = itemColors.majorSecondary
                )
            }
        }
    }
}


@[Preview Composable Suppress("MagicNumber")]
internal fun PassItemDetailsHistorySPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, shouldDisplayItemHistoryButton) = input

    PassTheme(isDark = isDark) {
        Surface {
            PassItemDetailsHistorySection(
                lastAutofillAtOption = Instant.fromEpochMilliseconds(1_684_213_366_026).toOption(),
                revision = 2,
                createdAt = Instant.fromEpochMilliseconds(1_697_213_366_026),
                modifiedAt = Instant.fromEpochMilliseconds(1_707_213_366_026),
                onViewItemHistoryClicked = {},
                itemColors = passItemColors(ItemCategory.Login),
                shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton
            )
        }
    }
}
