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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.reusepass.navigation.SecurityCenterReusedPassDestination
import proton.android.pass.features.security.center.reusepass.presentation.SecurityCenterReusedPassState
import proton.android.pass.features.security.center.shared.ui.bars.SecurityCenterTopBar
import proton.android.pass.features.security.center.shared.ui.headers.SecurityCenterListStickyHeader
import proton.android.pass.features.security.center.shared.ui.rows.SecurityCenterLoginItemRow

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SecurityCenterReusedPassContent(
    modifier: Modifier = Modifier,
    onNavigated: (SecurityCenterReusedPassDestination) -> Unit,
    state: SecurityCenterReusedPassState
) = with(state) {

    val isGroupCollapsed = rememberSaveable(saver = isCollapsedSaver()) { mutableStateListOf() }

    Scaffold(
        modifier = modifier,
        topBar = {
            SecurityCenterTopBar(
                modifier = Modifier
                    .padding(top = Spacing.medium - Spacing.extraSmall),
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
                .padding(top = Spacing.large)
        ) {
            reusedPasswords.forEach { reusedPassGroup ->
                stickyHeader {
                    SecurityCenterListStickyHeader(
                        onClick = {
                            if (isGroupCollapsed.contains(reusedPassGroup.key)) {
                                isGroupCollapsed.remove(reusedPassGroup.key)
                            } else {
                                isGroupCollapsed.add(reusedPassGroup.key)
                            }
                        },
                        isCollapsed = isGroupCollapsed.contains(reusedPassGroup.key),
                        label = {
                            SectionTitle(
                                text = stringResource(
                                    id = R.string.security_center_reused_pass_list_sticky_header_label,
                                    reusedPassGroup.reusedPasswordsCount
                                )
                            )
                        }
                    )
                }

                if (!isGroupCollapsed.contains(reusedPassGroup.key)) {
                    items(
                        items = reusedPassGroup.itemUiModels,
                        key = { itemUiModel -> itemUiModel.id.id }
                    ) { itemUiModel ->
                        SecurityCenterLoginItemRow(
                            itemUiModel = itemUiModel,
                            canLoadExternalImages = canLoadExternalImages,
                            shareIcon = getShareIcon(itemUiModel.shareId),
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
}

@Composable
private fun isCollapsedSaver() = listSaver<MutableList<String>, String>(
    save = {
        if (it.isNotEmpty()) {
            it.toList()
        } else {
            emptyList()
        }
    },
    restore = {
        it.toMutableStateList()
    }
)
