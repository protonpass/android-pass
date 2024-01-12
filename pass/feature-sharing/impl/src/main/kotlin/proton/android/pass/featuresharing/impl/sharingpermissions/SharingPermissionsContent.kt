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

package proton.android.pass.featuresharing.impl.sharingpermissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.setting.SettingRadio
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.featuresharing.impl.SharingNavigation
import proton.android.pass.featuresharing.impl.common.ShareToEmail

@Composable
fun SharingPermissionsContent(
    modifier: Modifier = Modifier,
    state: SharingPermissionsUIState,
    onNavigateEvent: (SharingNavigation) -> Unit,
    onPermissionsSubmit: () -> Unit,
    onPermissionChange: (String, SharingType) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = "",
                onUpClick = { onNavigateEvent(SharingNavigation.Back) },
                actions = {
                    CircleButton(
                        modifier = Modifier.padding(12.dp, 0.dp),
                        contentPadding = PaddingValues(16.dp, 10.dp),
                        color = PassTheme.colors.interactionNormMajor1,
                        onClick = onPermissionsSubmit
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
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.share_permissions_title),
                style = PassTheme.typography.heroNorm()
            )
            ShareToEmail(email = state.email)
            SettingRadio(
                modifier = Modifier.roundedContainer(
                    backgroundColor = Color.Transparent,
                    borderColor = if (state.sharingType == SharingType.Read) {
                        PassTheme.colors.interactionNormMajor1
                    } else {
                        PassTheme.colors.inputBorderNorm
                    }
                ),
                title = stringResource(R.string.sharing_can_view),
                subtitle = stringResource(R.string.sharing_can_view_description),
                isChecked = state.sharingType == SharingType.Read,
                colors = RadioButtonDefaults.colors(selectedColor = PassTheme.colors.interactionNormMajor1),
                onClick = { onPermissionChange(state.email, SharingType.Read) }
            )
            SettingRadio(
                modifier = Modifier.roundedContainer(
                    backgroundColor = Color.Transparent,
                    borderColor = if (state.sharingType == SharingType.Write) {
                        PassTheme.colors.interactionNormMajor1
                    } else {
                        PassTheme.colors.inputBorderNorm
                    }
                ),
                title = stringResource(R.string.sharing_can_edit),
                subtitle = stringResource(R.string.sharing_can_edit_description),
                isChecked = state.sharingType == SharingType.Write,
                colors = RadioButtonDefaults.colors(selectedColor = PassTheme.colors.interactionNormMajor1),
                onClick = { onPermissionChange(state.email, SharingType.Write) }
            )
            SettingRadio(
                modifier = Modifier.roundedContainer(
                    backgroundColor = Color.Transparent,
                    borderColor = if (state.sharingType == SharingType.Admin) {
                        PassTheme.colors.interactionNormMajor1
                    } else {
                        PassTheme.colors.inputBorderNorm
                    }
                ),
                title = stringResource(R.string.sharing_can_manage),
                subtitle = stringResource(R.string.sharing_can_manage_description),
                isChecked = state.sharingType == SharingType.Admin,
                colors = RadioButtonDefaults.colors(selectedColor = PassTheme.colors.interactionNormMajor1),
                onClick = { onPermissionChange(state.email, SharingType.Admin) }
            )
        }
    }
}
