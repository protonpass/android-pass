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

package proton.android.pass.features.itemdetail.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.itemdetail.login.widgets.LoginMonitorInsecurePassWidget
import proton.android.pass.features.itemdetail.login.widgets.LoginMonitorMissingTwoFaWidget
import proton.android.pass.features.itemdetail.login.widgets.LoginMonitorReusedPassWidget

@Composable
internal fun LoginMonitorSection(
    modifier: Modifier = Modifier,
    monitorState: LoginMonitorState,
    canLoadExternalImages: Boolean,
    onEvent: (LoginDetailEvent) -> Unit
) = with(monitorState) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        if (isPasswordInsecure) {
            LoginMonitorInsecurePassWidget()
        }

        if (isPasswordReused) {
            LoginMonitorReusedPassWidget(
                reusedPasswordDisplayMode = reusedPasswordDisplayMode,
                reusedPasswordCount = reusedPasswordCount,
                reusedPasswordItems = reusedPasswordItems,
                canLoadExternalImages = canLoadExternalImages,
                onEvent = onEvent
            )
        }

        if (isMissingTwoFa) {
            LoginMonitorMissingTwoFaWidget()
        }
    }
}
