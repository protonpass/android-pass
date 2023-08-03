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

package proton.android.pass.featuresharing.impl.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.featuresharing.impl.SharingNavigation

@Composable
fun ManageVaultContent(
    modifier: Modifier = Modifier,
    state: ManageVaultUiState,
    onNavigateEvent: (SharingNavigation) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = "",
                onUpClick = { onNavigateEvent(SharingNavigation.Back) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ManageVaultMembersList(
                modifier = Modifier.weight(1f),
                content = state.content,
                vault = state.vault,
                onMemberOptionsClick = {}
            )
            if (state.showShareButton) {
                CircleButton(
                    modifier = Modifier.fillMaxWidth(),
                    color = PassTheme.colors.interactionNormMajor1,
                    onClick = {
                        state.vault?.let { vault ->
                            onNavigateEvent(SharingNavigation.ShareVault(vault.vault.shareId))
                        }
                    }
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp),
                        text = stringResource(R.string.share_manage_vault_share_with_more_people),
                        color = PassTheme.colors.interactionNormContrast
                    )
                }
            }
        }
    }
}
