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

package proton.android.pass.features.security.center.reusepass.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.reusepass.navigation.SecurityCenterReusedPassDestination
import proton.android.pass.features.security.center.reusepass.presentation.SecurityCenterReusedPassState
import proton.android.pass.features.security.center.shared.ui.bars.SecurityCenterTopBar
import proton.android.pass.features.security.center.weakpass.ui.SecurityCenterWeakPassRow

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SecurityCenterReusedPassContent(
    modifier: Modifier = Modifier,
    onNavigated: (SecurityCenterReusedPassDestination) -> Unit,
    state: SecurityCenterReusedPassState
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SecurityCenterTopBar(
                modifier = Modifier
                    .padding(
                        start = Spacing.medium,
                        top = 12.dp,
                        end = Spacing.medium
                    ),
                title = stringResource(R.string.security_center_reused_pass_top_bar_title),
                subtitle = stringResource(R.string.security_center_reused_pass_top_bar_subtitle),
                onUpClick = { onNavigated(SecurityCenterReusedPassDestination.Back) }
            )
        }
    ) { innerPaddingValues ->
        LazyColumn(
            modifier = Modifier
                .background(PassTheme.colors.backgroundStrong)
                .padding(paddingValues = innerPaddingValues)
                .padding(top = Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            reusedPassGroups.forEach { reusedPassGroup ->
                stickyHeader {
                    SectionTitle(
                        text = stringResource(
                            id = R.string.security_center_reused_pass_list_sticky_header_label,
                            reusedPassGroup.reusedPasswordsCount
                        )
                    )
                }

                items(
                    items = reusedPassGroup.itemUiModels,
                    key = { itemUiModel -> itemUiModel.id.id }
                ) { itemUiModel ->
                    SecurityCenterWeakPassRow(
                        itemUiModel = itemUiModel,
                        canLoadExternalImages = canLoadExternalImages,
                        onClick = {
                            SecurityCenterReusedPassDestination.ItemDetails(
                                shareId = itemUiModel.shareId,
                                itemId = itemUiModel.id
                            ).also(onNavigated)
                        }
                    )
                }
            }
        }
    }
}
