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

package proton.android.pass.featuresettings.impl.defaultvault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.featuresettings.impl.R

@Composable
fun SelectDefaultVaultContents(
    modifier: Modifier = Modifier,
    vaults: ImmutableList<VaultEnabledPair>,
    loading: Boolean,
    onVaultSelected: (VaultWithItemCount) -> Unit
) {
    var vaultBeingUpdated: ShareId? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(loading) {
        if (!loading) {
            vaultBeingUpdated = null
        }
    }

    BottomSheetItemList(
        modifier = modifier,
        items = vaults.map { vaultEnabledPair ->
            val vaultItemCount = vaultEnabledPair.vault
            BottomSheetVaultRow(
                vault = vaultEnabledPair.vault,
                isSelected = false,
                enabled = !loading && vaultEnabledPair.enabled,
                customSubtitle = if (!vaultEnabledPair.enabled) {
                    stringResource(R.string.settings_default_vault_disabled_reason)
                } else null,
                isLoading = vaultItemCount.vault.shareId == vaultBeingUpdated,
                onVaultClick = {
                    vaultBeingUpdated = vaultItemCount.vault.shareId
                    onVaultSelected(vaultItemCount)
                }
            )
        }
            .withDividers()
            .toImmutableList(),
    )

}
