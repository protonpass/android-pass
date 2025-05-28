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

package proton.android.pass.composecomponents.impl.item.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailBannerRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailTitleRow
import proton.android.pass.composecomponents.impl.item.details.sections.PassItemDetailSections
import proton.android.pass.composecomponents.impl.utils.PassItemColors

@Composable
fun PassItemDetailsContent(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    itemDetailState: ItemDetailState,
    itemColors: PassItemColors,
    onEvent: (PassItemDetailsUiEvent) -> Unit,
    shouldDisplayItemHistorySection: Boolean,
    shouldDisplayItemHistoryButton: Boolean,
    shouldDisplayFileAttachments: Boolean,
    shouldDisplayCustomItems: Boolean,
    extraBottomSpacing: Dp = Spacing.none
) {
    Scaffold(
        modifier = modifier,
        topBar = { topBar() }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = PassTheme.colors.itemDetailBackground)
                .padding(paddingValues = innerPaddingValues)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
        ) {
            PassItemDetailBannerRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium),
                itemDetailState = itemDetailState,
                onEvent = onEvent
            )
            PassItemDetailTitleRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.medium,
                        vertical = Spacing.small
                    ),
                itemDetailState = itemDetailState,
                itemColors = itemColors,
                onEvent = onEvent
            )

            PassItemDetailSections(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium),
                itemDetailState = itemDetailState,
                itemColors = itemColors,
                onEvent = onEvent,
                shouldDisplayItemHistorySection = shouldDisplayItemHistorySection,
                shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton,
                shouldDisplayFileAttachments = shouldDisplayFileAttachments,
                shouldDisplayCustomItems = shouldDisplayCustomItems
            )

            Spacer(
                modifier = Modifier.height(height = extraBottomSpacing)
            )
        }
    }
}
