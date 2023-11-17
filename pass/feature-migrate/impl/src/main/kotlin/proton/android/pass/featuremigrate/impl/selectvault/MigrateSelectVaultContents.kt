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

package proton.android.pass.featuremigrate.impl.selectvault

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.domain.ShareId

@Composable
fun MigrateSelectVaultContents(
    modifier: Modifier = Modifier,
    vaults: ImmutableList<VaultEnabledPair>,
    onVaultSelected: (ShareId) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier,
        items = vaults.map { vault ->
            BottomSheetVaultRow(
                vault = vault.vault,
                isSelected = false,
                enabled = vault.isEnabled,
                onVaultClick = { onVaultSelected(vault.vault.vault.shareId) }
            )
        }
            .withDividers()
            .toImmutableList(),
    )
}
