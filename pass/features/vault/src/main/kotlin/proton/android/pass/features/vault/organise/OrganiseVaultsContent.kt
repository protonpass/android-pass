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

package proton.android.pass.features.vault.organise

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.R
import proton.android.pass.composecomponents.impl.R as CompR

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganiseVaultsContent(
    modifier: Modifier = Modifier,
    state: OrganiseVaultsUIState,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    onVisibilityChange: (ShareId, Boolean) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.Cross,
                title = stringResource(R.string.organise_vaults_title),
                onUpClick = onClose,
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(vertical = Spacing.small),
                        isLoading = state.isSubmitLoading.value(),
                        buttonEnabled = state.areTherePendingChanges && !state.isSubmitLoading.value(),
                        color = if (!state.isSubmitLoading.value()) {
                            PassTheme.colors.interactionNormMajor1
                        } else {
                            PassTheme.colors.interactionNormMinor1
                        },
                        text = {
                            Text.Body2Regular(
                                text = stringResource(id = CompR.string.action_confirm),
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = onConfirm
                    )
                }
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .background(PassTheme.colors.backgroundStrong)
                .padding(contentPadding)
        ) {
            if (state.visibleVaults.isNotEmpty()) {
                stickyHeader(key = "visible_vaults") {
                    Text.Body2Bold(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PassTheme.colors.backgroundStrong)
                            .padding(horizontal = Spacing.medium)
                            .padding(top = Spacing.medium),
                        text = stringResource(R.string.organise_vaults_visible_title)
                    )
                }
            }
            itemsIndexed(
                items = state.visibleVaults,
                key = { _, vault -> vault.vault.shareId.id },
                itemContent = { index, (vault, itemCount) ->
                    OrganiseVaultsRow(
                        modifier = Modifier.animateItem(),
                        shareId = vault.shareId,
                        shareIconRes = vault.icon.toResource(),
                        iconColor = vault.color.toColor(),
                        iconBackgroundColor = vault.color.toColor(isBackground = true),
                        name = vault.name,
                        itemsCount = itemCount.toInt(),
                        isSelected = true,
                        onClick = onVisibilityChange
                    )
                    if (index < state.visibleVaults.size - 1) {
                        PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
                    }
                }
            )
            if (state.hiddenVaults.isNotEmpty()) {
                stickyHeader(key = "hidden_vaults") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PassTheme.colors.backgroundStrong)
                            .padding(horizontal = Spacing.medium)
                            .padding(top = Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Text.Body2Bold(stringResource(R.string.organise_vaults_hidden_title))
                        Text.Body2Weak(
                            stringResource(R.string.organise_vaults_hidden_description)
                        )
                    }
                }
            }
            itemsIndexed(
                items = state.hiddenVaults,
                key = { _, vault -> vault.vault.shareId.id },
                itemContent = { index, (vault, itemCount) ->
                    OrganiseVaultsRow(
                        modifier = Modifier.animateItem(),
                        shareId = vault.shareId,
                        shareIconRes = vault.icon.toResource(),
                        iconColor = vault.color.toColor(),
                        iconBackgroundColor = vault.color.toColor(isBackground = true),
                        name = vault.name,
                        itemsCount = itemCount.toInt(),
                        isSelected = false,
                        onClick = onVisibilityChange
                    )
                    if (index < state.hiddenVaults.size - 1) {
                        PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
                    }
                }
            )

            item {
                Spacer(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    }
}

