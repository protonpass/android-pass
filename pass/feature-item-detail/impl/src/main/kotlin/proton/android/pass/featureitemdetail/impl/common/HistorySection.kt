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

package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import me.proton.core.compose.component.ProtonButton
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.ProtonHistoryItemRow
import proton.android.pass.featureitemdetail.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun HistorySection(
    modifier: Modifier = Modifier,
    createdInstant: Instant,
    modifiedInstant: Instant,
    onViewItemHistoryClicked: () -> Unit,
    buttonBackgroundColor: Color,
    buttonTextColor: Color,
) {
    RoundedCornersColumn(
        modifier = modifier,
    ) {
        ProtonHistoryItemRow(
            leadingIcon = painterResource(CoreR.drawable.ic_proton_pencil),
            title = stringResource(id = R.string.item_detail_history_modified_last),
            subtitle = formatMoreInfoInstantText(
                now = Instant.fromEpochSeconds(0),
                toFormat = modifiedInstant,
            ),
            paddingValues = PaddingValues(
                top = Spacing.medium,
                start = Spacing.medium,
                bottom = Spacing.small,
            ),
        )

        ProtonHistoryItemRow(
            leadingIcon = painterResource(CoreR.drawable.ic_proton_bolt),
            title = stringResource(id = R.string.item_detail_history_created),
            subtitle = formatMoreInfoInstantText(
                now = Instant.fromEpochSeconds(0),
                toFormat = createdInstant,
            ),
            paddingValues = PaddingValues(
                top = Spacing.small,
                start = Spacing.medium,
                bottom = Spacing.medium,
            ),
        )

        ProtonButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.medium,
                    end = Spacing.medium,
                    bottom = Spacing.medium,
                ),
            onClick = onViewItemHistoryClicked,
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(buttonBackgroundColor),
            contentPadding = PaddingValues(
                horizontal = 12.dp,
                vertical = 12.dp,
            ),
            elevation = null,
            border = null,
        ) {
            Text(
                text = stringResource(id = R.string.item_detail_history_button_view),
                fontSize = 16.sp,
                color = buttonTextColor,
            )
        }
    }
}

@[Preview Composable Suppress("MagicNumber")]
fun HistorySectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            HistorySection(
                createdInstant = Instant.fromEpochMilliseconds(1_697_213_366_026),
                modifiedInstant = Instant.fromEpochMilliseconds(1_707_213_366_026),
                onViewItemHistoryClicked = {},
                buttonBackgroundColor = PassTheme.colors.loginInteractionNormMinor2,
                buttonTextColor = PassTheme.colors.loginInteractionNormMajor2,
            )
        }
    }
}
