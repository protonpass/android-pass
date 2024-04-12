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

package proton.android.pass.features.security.center.darkweb.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebUiState
import proton.android.pass.features.security.center.darkweb.ui.customemails.list.CustomEmailsList
import proton.android.pass.features.security.center.darkweb.ui.summary.DarkWebSummary
import proton.android.pass.features.security.center.shared.ui.bars.SecurityCenterTopBar

@Composable
internal fun DarkWebContent(
    modifier: Modifier = Modifier,
    state: DarkWebUiState,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            val subtitle = state.lastCheckTime.value()?.let { lastCheckTime ->
                stringResource(
                    id = R.string.security_center_dark_web_monitor_top_bar_subtitle,
                    lastCheckTime
                )
            }
            SecurityCenterTopBar(
                title = stringResource(R.string.security_center_dark_web_monitor_top_bar_title),
                subtitle = subtitle,
                onUpClick = { onEvent(DarkWebUiEvent.OnUpClick) },
                actions = { DarkWebStatusIndicator(status = state.darkWebStatus) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            DarkWebSummary()
            CustomEmailsList(
                state = state.customEmails,
                onEvent = onEvent
            )
        }
    }
}
