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

package proton.android.pass.features.item.history.restore.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passFormattedDateText
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.item.history.R

private const val TAB_COUNT = 2
private const val TAB_INDEX_CURRENT = 1

@Composable
internal fun ItemHistoryRestoreTabRow(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    itemColors: PassItemColors,
    revisionTime: Long,
    onSelectTab: (Int) -> Unit
) {
    TabRow(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(size = Radius.large)
            )
            .clip(shape = RoundedCornerShape(size = Radius.large)),
        selectedTabIndex = selectedTabIndex,
        indicator = {},
        divider = {}
    ) {
        repeat(TAB_COUNT) { index ->
            val isSelected = selectedTabIndex == index

            Tab(
                modifier = Modifier
                    .background(color = PassTheme.colors.backgroundMedium)
                    .clip(shape = RoundedCornerShape(size = Radius.large))
                    .applyIf(
                        condition = isSelected,
                        ifTrue = {
                            background(color = itemColors.majorPrimary)
                        }
                    ),
                selected = isSelected,
                onClick = { onSelectTab(index) },
                text = {
                    Text(
                        text = if (index == TAB_INDEX_CURRENT) {
                            stringResource(id = R.string.item_history_restore_tab_title_current)
                        } else {
                            passFormattedDateText(endInstant = Instant.fromEpochSeconds(revisionTime))
                        },
                        style = PassTheme.typography.body3Norm(),
                        color = if (isSelected) {
                            PassTheme.colors.textInvert
                        } else {
                            PassTheme.colors.textNorm
                        }
                    )
                }
            )
        }
    }
}

@[Preview Composable]
internal fun ItemHistoryRestoreTabRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ItemHistoryRestoreTabRow(
                selectedTabIndex = 0,
                itemColors = passItemColors(itemCategory = ItemCategory.Login),
                revisionTime = 1_721_125_029L,
                onSelectTab = {}
            )
        }
    }
}
