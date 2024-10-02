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

package proton.android.pass.features.sharing.sharingpermissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.SharingNavigation

@Composable
fun SharingPermissionsContent(
    modifier: Modifier = Modifier,
    state: SharingPermissionsUIState,
    onNavigateEvent: (SharingNavigation) -> Unit,
    onEvent: (SharingPermissionsUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = "",
                onUpClick = { onNavigateEvent(SharingNavigation.Back) },
                actions = {
                    CircleButton(
                        modifier = Modifier.padding(horizontal = Spacing.mediumSmall, vertical = Spacing.none),
                        contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = 10.dp),
                        color = PassTheme.colors.interactionNormMajor1,
                        onClick = { onEvent(SharingPermissionsUiEvent.OnSubmit) }
                    ) {
                        Text(
                            text = stringResource(R.string.share_continue),
                            style = PassTheme.typography.body3Norm(),
                            color = PassTheme.colors.textInvert
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.share_permissions_title),
                style = PassTheme.typography.heroNorm()
            )

            SharingPermissionsHeader(
                state = state.headerState,
                onSetAllClick = { onEvent(SharingPermissionsUiEvent.OnSetAllPermissionsClick) }
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = state.addresses, key = { it.address }) { addressPermission ->
                    SharingPermissionItem(
                        modifier = Modifier.padding(vertical = 8.dp),
                        address = addressPermission,
                        onPermissionChangeClick = {
                            onEvent(
                                SharingPermissionsUiEvent.OnPermissionChangeClick(addressPermission)
                            )
                        }
                    )
                }
            }
        }
    }
}
