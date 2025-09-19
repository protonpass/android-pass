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

package proton.android.pass.features.home.drawer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R as CoreR
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.home.R
import proton.android.pass.features.home.drawer.presentation.HomeDrawerState

@Composable
internal fun HomeDrawerContent(
    modifier: Modifier = Modifier,
    state: HomeDrawerState,
    onUiEvent: (HomeDrawerUiEvent) -> Unit
) = with(state) {
    Column(
        modifier = modifier
            .background(color = PassTheme.colors.backgroundWeak)
            .statusBarsPadding()
            .padding(top = Spacing.small)
    ) {
        HomeDrawerList(
            modifier = Modifier
                .fillMaxHeight()
                .weight(weight = 1f, fill = true),
            vaultShares = vaultShares.toPersistentList(),
            vaultSharesItemsCount = vaultSharesItemsCounter.toPersistentMap(),
            vaultSelectionOption = vaultSelectionOption,
            allItemsCount = allItemsCount,
            hasSharedWithMeItems = hasSharedWithMeItems,
            sharedWithMeItemsCount = sharedWithMeItemsCount,
            hasSharedByMeItems = hasSharedByMeItems,
            sharedByMeItemsCount = sharedByMeItemsCount,
            trashedItemsCount = trashedItemsCount,
            onUiEvent = onUiEvent
        )

        HomeDrawerFooter(
            canCreateVaults = canCreateVaults,
            canOrganiseVaults = canOrganiseVaults,
            onUiEvent = onUiEvent
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HomeDrawerFooter(
    canCreateVaults: Boolean,
    canOrganiseVaults: Boolean,
    onUiEvent: (HomeDrawerUiEvent) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center
    ) {
        if (canCreateVaults) {
            Button.Circular(
                modifier = Modifier.padding(Spacing.small),
                color = PassTheme.colors.interactionNormMinor1,
                contentPadding = PaddingValues(Spacing.mediumSmall),
                onClick = {
                    onUiEvent(HomeDrawerUiEvent.OnCreateVaultClick)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Icon.Default(
                        modifier = Modifier.size(ProtonTheme.typography.body1Regular.fontSize.value.dp),
                        id = CoreR.drawable.ic_proton_plus,
                        tint = PassTheme.colors.interactionNormMajor2
                    )
                    Text.Body1Regular(
                        text = stringResource(R.string.vault_drawer_create_vault),
                        color = PassTheme.colors.interactionNormMajor2
                    )
                }
            }
        }
        if (canOrganiseVaults) {
            Button.Circular(
                modifier = Modifier.padding(Spacing.small),
                color = PassTheme.colors.interactionNormMinor1,
                contentPadding = PaddingValues(Spacing.mediumSmall),
                onClick = {
                    onUiEvent(HomeDrawerUiEvent.OnOrganiseVaultsClick)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Icon.Default(
                        modifier = Modifier.size(ProtonTheme.typography.body1Regular.fontSize.value.dp),
                        id = CoreR.drawable.ic_proton_list_bullets,
                        tint = PassTheme.colors.interactionNormMajor2
                    )
                    Text.Body1Regular(
                        text = stringResource(R.string.vault_drawer_organise_vaults),
                        color = PassTheme.colors.interactionNormMajor2
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun FooterPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            HomeDrawerFooter(
                canCreateVaults = true,
                canOrganiseVaults = true,
                {}
            )
        }
    }
}
