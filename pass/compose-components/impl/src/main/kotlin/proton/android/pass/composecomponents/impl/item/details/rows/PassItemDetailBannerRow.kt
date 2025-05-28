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

package proton.android.pass.composecomponents.impl.item.details.rows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.sections.login.widgets.LoginMonitorSection

@Composable
internal fun PassItemDetailBannerRow(
    modifier: Modifier = Modifier,
    itemDetailState: ItemDetailState,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(itemDetailState) {
    when (val currentState = this) {
        is ItemDetailState.Login -> {
            AnimatedVisibility(
                modifier = modifier,
                visible = currentState.loginMonitorState.shouldDisplayMonitoring
            ) {
                LoginMonitorSection(
                    monitorState = currentState.loginMonitorState,
                    canLoadExternalImages = canLoadExternalImages,
                    onEvent = onEvent
                )
            }
        }

        is ItemDetailState.Alias,
        is ItemDetailState.CreditCard,
        is ItemDetailState.Identity,
        is ItemDetailState.Note,
        is ItemDetailState.WifiNetwork,
        is ItemDetailState.SSHKey,
        is ItemDetailState.Custom,
        is ItemDetailState.Unknown -> {}
    }
}
