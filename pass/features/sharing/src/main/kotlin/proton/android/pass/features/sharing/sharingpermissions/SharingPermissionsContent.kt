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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.sharing.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SharingPermissionsContent(
    modifier: Modifier = Modifier,
    state: SharingPermissionsUIState,
    onEvent: (SharingPermissionsUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.BackArrow,
                title = stringResource(R.string.share_permissions_title),
                onUpClick = { onEvent(SharingPermissionsUiEvent.OnBackClick) },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(vertical = Spacing.small),
                        isLoading = false,
                        color = PassTheme.colors.interactionNormMajor1,
                        text = {
                            Text.Body2Regular(
                                text = stringResource(id = CompR.string.action_continue),
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = { onEvent(SharingPermissionsUiEvent.OnSubmit) }
                    )
                }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPaddingValues)
                .padding(all = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            SharingPermissionsHeader(
                modifier = Modifier.padding(top = Spacing.medium),
                memberCount = state.memberCount,
                onSetAllClick = { onEvent(SharingPermissionsUiEvent.OnSetAllPermissionsClick) }
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = state.addresses, key = { it.address }) { addressPermission ->
                    SharingPermissionItem(
                        modifier = Modifier.padding(vertical = Spacing.small),
                        address = addressPermission,
                        isRenameAdminToManagerEnabled = state.isRenameAdminToManagerEnabled,
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
