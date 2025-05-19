/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.sharing.sharingsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.PassItemIcon
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.sharing.R

@Composable
internal fun SharingSummaryContent(
    modifier: Modifier = Modifier,
    state: SharingSummaryState,
    onUiEvent: (SharingSummaryUiEvent) -> Unit
) {
    val actionTextRes = remember(state) {
        when (state) {
            SharingSummaryState.Initial -> null
            is SharingSummaryState.ShareItem -> R.string.share_summary_share_item
            is SharingSummaryState.ShareVault -> R.string.share_summary_share_vault
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.BackArrow,
                title = stringResource(R.string.share_summary_title),
                onUpClick = { onUiEvent(SharingSummaryUiEvent.OnBackClick) },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(vertical = Spacing.small),
                        isLoading = state.isLoading,
                        color = PassTheme.colors.interactionNormMajor1,
                        text = {
                            Text.Body2Regular(
                                text = actionTextRes
                                    ?.let { resId -> stringResource(id = resId) }
                                    .orEmpty(),
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = {
                            when (state) {
                                SharingSummaryState.Initial -> null

                                is SharingSummaryState.ShareItem -> SharingSummaryUiEvent.OnShareItemClick(
                                    itemId = state.itemId,
                                    itemCategory = state.itemCategory
                                )

                                is SharingSummaryState.ShareVault -> SharingSummaryUiEvent.OnShareVaultClick
                            }?.let(onUiEvent)
                        }
                    )
                }
            )
        }
    ) { innerPaddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues = innerPaddingValues)
                .padding(all = Spacing.medium)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(space = Spacing.large)
                ) {
                    when (state) {
                        SharingSummaryState.Initial -> Unit
                        is SharingSummaryState.ShareItem -> {
                            SharingSummaryShareSection(
                                modifier = Modifier.padding(bottom = Spacing.medium),
                                sectionTitle = stringResource(R.string.share_summary_item_title),
                                shareTitle = state.itemTitle,
                                shareSubTitle = state.itemSubtitle,
                                shareIcon = {
                                    PassItemIcon(
                                        itemCategory = state.itemCategory,
                                        text = state.itemTitle,
                                        website = state.itemWebsite,
                                        packageName = state.itemPackageName,
                                        canLoadExternalImages = state.canItemLoadExternalImages
                                    )
                                }
                            )
                        }

                        is SharingSummaryState.ShareVault -> {
                            SharingSummaryShareSection(
                                modifier = Modifier.padding(bottom = Spacing.medium),
                                sectionTitle = stringResource(R.string.share_summary_vault_title),
                                shareTitle = state.vault.name,
                                shareSubTitle = pluralStringResource(
                                    R.plurals.sharing_item_count,
                                    state.vaultItemCount,
                                    state.vaultItemCount
                                ),
                                shareIcon = {
                                    VaultIcon(
                                        icon = state.vault.icon.toResource(),
                                        iconColor = state.vault.color.toColor(),
                                        backgroundColor = state.vault.color.toColor(isBackground = true)
                                    )
                                }
                            )
                        }
                    }
                }

                Text.Body1Regular(
                    modifier = Modifier.padding(bottom = Spacing.small),
                    text = stringResource(R.string.share_with_title),
                    color = PassTheme.colors.textWeak
                )
            }

            items(items = state.addresses, key = { it.address }) { address ->
                AddressRowSection(
                    modifier = Modifier.padding(vertical = Spacing.small),
                    address = address,
                    isRenameAdminToManagerEnabled = state.isRenameAdminToManagerEnabled
                )
            }

            if (state.addresses.isNotEmpty()) {
                item {
                    SharingSummaryAccessLevelSection(
                        modifier = Modifier.padding(top = Spacing.medium),
                        shareType = state.shareType
                    )
                }
            }
        }
    }
}
