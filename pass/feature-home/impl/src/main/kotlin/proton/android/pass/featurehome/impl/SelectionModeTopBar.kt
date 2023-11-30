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

package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption

@Composable
fun SelectionModeTopBar(
    modifier: Modifier = Modifier,
    homeVaultSelection: VaultSelectionOption,
    onEvent: (HomeUiEvent) -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier.padding(vertical = 12.dp),
        title = { },
        navigationIcon = {
        },
        actions = {
            if (homeVaultSelection == VaultSelectionOption.Trash) {
                IconButton(onClick = { onEvent(HomeUiEvent.RestoreItemsActionClick) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_proton_clock_rotate_left),
                        contentDescription = null,
                        tint = PassTheme.colors.textWeak
                    )
                }
                IconButton(onClick = { onEvent(HomeUiEvent.PermanentlyDeleteItemsActionClick) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_proton_trash_cross),
                        contentDescription = null,
                        tint = PassTheme.colors.textWeak
                    )
                }
            } else {
                IconButton(onClick = { onEvent(HomeUiEvent.MoveItemsActionClick) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_proton_folder_arrow_in),
                        contentDescription = null,
                        tint = PassTheme.colors.textWeak
                    )
                }
                IconButton(onClick = { onEvent(HomeUiEvent.DeleteItemsActionClick) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_proton_trash),
                        contentDescription = null,
                        tint = PassTheme.colors.textWeak
                    )
                }
            }
        }
    )
}
