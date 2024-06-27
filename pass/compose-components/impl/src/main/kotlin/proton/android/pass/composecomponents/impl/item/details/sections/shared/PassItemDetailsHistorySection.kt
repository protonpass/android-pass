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
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.PassHistoryItemRow
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passFormattedDateText
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import me.proton.core.presentation.R as CoreR

@Composable
internal fun PassItemDetailsHistorySection(
    modifier: Modifier = Modifier,
    createdAt: Instant,
    modifiedAt: Instant,
    onEvent: (PassItemDetailsUiEvent) -> Unit,
    itemColors: PassItemColors
) {
    RoundedCornersColumn(
        modifier = modifier
    ) {
        PassHistoryItemRow(
            leadingIcon = CoreR.drawable.ic_proton_pencil,
            title = stringResource(id = R.string.item_details_shared_section_item_history_modified_title),
            subtitle = passFormattedDateText(
                startInstant = Instant.fromEpochSeconds(0),
                endInstant = modifiedAt
            ),
            paddingValues = PaddingValues(
                top = Spacing.medium,
                start = Spacing.medium,
                bottom = Spacing.small
            )
        )

        PassHistoryItemRow(
            leadingIcon = CoreR.drawable.ic_proton_bolt,
            title = stringResource(id = R.string.item_details_shared_section_item_history_created_title),
            subtitle = passFormattedDateText(
                startInstant = Instant.fromEpochSeconds(0),
                endInstant = createdAt
            ),
            paddingValues = PaddingValues(
                top = Spacing.small,
                start = Spacing.medium,
                bottom = Spacing.medium
            )
        )

        ProtonButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.medium,
                    end = Spacing.medium,
                    bottom = Spacing.medium
                ),
            onClick = { onEvent(PassItemDetailsUiEvent.OnViewItemHistoryClick) },
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


@[Preview Composable Suppress("MagicNumber")]
internal fun PassItemDetailsHistorySectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            PassItemDetailsHistorySection(
                createdAt = Instant.fromEpochMilliseconds(1_697_213_366_026),
                modifiedAt = Instant.fromEpochMilliseconds(1_707_213_366_026),
                onEvent = {},
                itemColors = passItemColors(ItemCategory.Login)
            )
        }
    }
}
