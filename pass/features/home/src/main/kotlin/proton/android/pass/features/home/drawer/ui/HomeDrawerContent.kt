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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
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
            sharedWithMeItemsCount = sharedWithMeItemsCount,
            sharedByMeItemsCount = sharedByMeItemsCount,
            trashedItemsCount = trashedItemsCount,
            onUiEvent = onUiEvent
        )

        if (canCreateVaults) {
            PassCircleButton(
                modifier = Modifier.padding(all = Spacing.medium),
                text = stringResource(R.string.vault_drawer_create_vault),
                textColor = PassTheme.colors.interactionNormMajor2,
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                onClick = {
                    onUiEvent(HomeDrawerUiEvent.OnCreateVaultClick)
                }
            )
        }
    }
}
