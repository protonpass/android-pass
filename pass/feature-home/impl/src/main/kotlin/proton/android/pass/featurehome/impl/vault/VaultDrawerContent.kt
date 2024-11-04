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

package proton.android.pass.featurehome.impl.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import me.proton.core.compose.theme.ProtonDimens
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.domain.ShareId
import proton.android.pass.featurehome.impl.R
import proton.android.pass.searchoptions.api.VaultSelectionOption

@Composable
fun VaultDrawerContent(
    modifier: Modifier = Modifier,
    homeVaultSelection: VaultSelectionOption,
    list: ImmutableList<ShareUiModelWithItemCount>,
    totalTrashedItems: Long,
    canCreateVault: Boolean,
    onAllVaultsClick: () -> Unit,
    onVaultClick: (ShareId) -> Unit,
    onVaultOptionsClick: (ShareUiModelWithItemCount) -> Unit,
    onTrashClick: () -> Unit,
    onCreateVaultClick: () -> Unit
) {
    Column(modifier = modifier.background(PassTheme.colors.backgroundWeak)) {
        VaultDrawerSection(
            modifier = Modifier
                .padding(top = ProtonDimens.DefaultSpacing)
                .weight(1f, fill = true),
            homeVaultSelection = homeVaultSelection,
            list = list,
            totalTrashedItems = totalTrashedItems,
            onVaultOptionsClick = onVaultOptionsClick,
            onAllVaultsClick = onAllVaultsClick,
            onVaultClick = onVaultClick,
            onTrashClick = onTrashClick
        )

        if (canCreateVault) {
            CircleButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.medium),
                contentPadding = PaddingValues(14.dp),
                color = PassTheme.colors.loginInteractionNormMinor1,
                elevation = ButtonDefaults.elevation(0.dp),
                onClick = onCreateVaultClick
            ) {
                Text(
                    text = stringResource(R.string.vault_drawer_create_vault),
                    color = PassTheme.colors.loginInteractionNormMajor2,
                    style = PassTheme.typography.body3Norm()
                )
            }
        }
    }
}
