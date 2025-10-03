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

package proton.android.pass.features.security.center.excludeditems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.excludeditems.presentation.SecurityCenterExcludedItemsState
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterLoginItemRow

@Composable
internal fun SecurityCenterExcludedItemsContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecurityCenterExcludedItemsUiEvent) -> Unit,
    state: SecurityCenterExcludedItemsState
) = with(state) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            PassExtendedTopBar(
                modifier = Modifier
                    .padding(top = Spacing.medium - Spacing.extraSmall),
                title = stringResource(R.string.security_center_excluded_items_top_bar_title),
                onUpClick = { onUiEvent(SecurityCenterExcludedItemsUiEvent.Back) }
            )
        }
    ) { innerPaddingValues ->
        LazyColumn(
            modifier = Modifier
                .background(PassTheme.colors.backgroundStrong)
                .padding(paddingValues = innerPaddingValues)
                .padding(top = Spacing.large)
        ) {
            items(
                items = excludedItemUiModels,
                key = { it.key }
            ) { itemUiModel ->
                SecurityCenterLoginItemRow(
                    itemUiModel = itemUiModel,
                    canLoadExternalImages = canLoadExternalImages,
                    shareIcon = getShareIcon(itemUiModel.shareId),
                    onClick = {
                        SecurityCenterExcludedItemsUiEvent.OnShowItemDetails(
                            shareId = itemUiModel.shareId,
                            itemId = itemUiModel.id
                        ).also(onUiEvent)
                    }
                )
            }
        }
    }
}
