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

package proton.android.pass.composecomponents.impl.item.details.rows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.composecomponents.impl.badge.CircledBadge
import proton.android.pass.composecomponents.impl.badge.OverlayBadge
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.titles.PassItemDetailSubtitle
import proton.android.pass.composecomponents.impl.item.details.titles.PassItemDetailTitle
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.CustomIcon
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId

@Composable
internal fun PassItemDetailTitleRow(
    modifier: Modifier = Modifier,
    itemDetailState: ItemDetailState,
    itemColors: PassItemColors,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(itemDetailState) {
    when (this) {
        is ItemDetailState.Alias -> {
            ItemDetailTitleRow(
                modifier = modifier,
                title = itemContents.title,
                isPinned = isItemPinned,
                itemColors = itemColors,
                share = itemShare,
                itemDiffType = itemDetailState.itemDiffs.title,
                onSharedVaultClick = { sharedVaultId ->
                    PassItemDetailsUiEvent.OnSharedVaultClick(
                        sharedVaultId = sharedVaultId
                    ).also(onEvent)
                }
            ) {
                AliasIcon(
                    size = 60,
                    shape = PassTheme.shapes.squircleMediumLargeShape,
                    activeAlias = itemContents.isEnabled
                )
            }
        }

        is ItemDetailState.CreditCard -> {
            ItemDetailTitleRow(
                modifier = modifier,
                title = itemContents.title,
                isPinned = isItemPinned,
                itemColors = itemColors,
                share = itemShare,
                itemDiffType = itemDetailState.itemDiffs.title,
                onSharedVaultClick = { sharedVaultId ->
                    PassItemDetailsUiEvent.OnSharedVaultClick(
                        sharedVaultId = sharedVaultId
                    ).also(onEvent)
                }
            ) {
                CreditCardIcon(
                    size = 60,
                    shape = PassTheme.shapes.squircleMediumLargeShape
                )
            }
        }

        is ItemDetailState.Identity -> {
            ItemDetailTitleRow(
                modifier = modifier,
                title = itemContents.title,
                isPinned = isItemPinned,
                itemColors = itemColors,
                share = itemShare,
                itemDiffType = itemDetailState.itemDiffs.title,
                onSharedVaultClick = { sharedVaultId ->
                    PassItemDetailsUiEvent.OnSharedVaultClick(
                        sharedVaultId = sharedVaultId
                    ).also(onEvent)
                }
            ) {
                IdentityIcon(
                    size = 60,
                    shape = PassTheme.shapes.squircleMediumLargeShape
                )
            }
        }

        is ItemDetailState.Login -> {
            ItemDetailTitleRow(
                modifier = modifier,
                title = itemContents.title,
                isPinned = isItemPinned,
                itemColors = itemColors,
                share = itemShare,
                itemDiffType = itemDetailState.itemDiffs.title,
                onSharedVaultClick = { sharedVaultId ->
                    PassItemDetailsUiEvent.OnSharedVaultClick(
                        sharedVaultId = sharedVaultId
                    ).also(onEvent)
                }
            ) {
                LoginIcon(
                    size = 60,
                    shape = PassTheme.shapes.squircleMediumLargeShape,
                    text = itemContents.title,
                    website = itemContents.websiteUrl,
                    packageName = itemContents.packageName,
                    canLoadExternalImages = canLoadExternalImages
                )
            }
        }

        is ItemDetailState.Note -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(Spacing.large)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        AnimatedVisibility(
                            visible = isItemPinned,
                            enter = expandHorizontally()
                        ) {
                            CircledBadge(
                                ratio = 1f,
                                backgroundColor = itemColors.majorPrimary
                            )
                        }

                        PassItemDetailTitle(
                            text = itemContents.title,
                            maxLines = Int.MAX_VALUE,
                            itemDiffType = itemDiffs.title
                        )
                    }

                    PassItemDetailSubtitle(
                        share = itemShare,
                        onClick = {
                            PassItemDetailsUiEvent.OnSharedVaultClick(
                                sharedVaultId = itemShare.id
                            ).also(onEvent)
                        }
                    )
                }
            }
        }

        is ItemDetailState.Custom -> {
            ItemDetailTitleRow(
                modifier = modifier,
                title = itemContents.title,
                isPinned = isItemPinned,
                itemColors = itemColors,
                share = itemShare,
                itemDiffType = itemDetailState.itemDiffs.title,
                onSharedVaultClick = { sharedVaultId ->
                    PassItemDetailsUiEvent.OnSharedVaultClick(
                        sharedVaultId = sharedVaultId
                    ).also(onEvent)
                }
            ) {
                CustomIcon(
                    size = 60,
                    shape = PassTheme.shapes.squircleMediumLargeShape
                )
            }
        }

        is ItemDetailState.Unknown -> {
            ItemDetailTitleRow(
                modifier = modifier,
                title = itemContents.title,
                isPinned = false,
                itemColors = itemColors,
                share = itemShare,
                itemDiffType = itemDetailState.itemDiffs.title,
                onSharedVaultClick = {},
                iconContent = {}
            )
        }
    }
}

@Composable
private fun ItemDetailTitleRow(
    modifier: Modifier = Modifier,
    title: String,
    isPinned: Boolean,
    itemColors: PassItemColors,
    share: Share,
    itemDiffType: ItemDiffType,
    onSharedVaultClick: (ShareId) -> Unit,
    iconContent: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        OverlayBadge(
            isShown = isPinned,
            badge = {
                CircledBadge(
                    backgroundColor = itemColors.majorPrimary
                )
            },
            content = { iconContent() }
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            PassItemDetailTitle(
                text = title,
                itemDiffType = itemDiffType
            )

            PassItemDetailSubtitle(
                share = share,
                onClick = { onSharedVaultClick(share.id) }
            )
        }
    }
}
