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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebUiState
import proton.android.pass.features.security.center.darkweb.ui.customemails.list.CustomEmailsHeader
import proton.android.pass.features.security.center.darkweb.ui.customemails.list.CustomEmailsList
import proton.android.pass.features.security.center.darkweb.ui.summary.DarkWebSummaryType
import proton.android.pass.features.security.center.darkweb.ui.summary.EmailBreachSection

@Composable
internal fun DarkWebContent(
    modifier: Modifier = Modifier,
    state: DarkWebUiState,
    onEvent: (DarkWebUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            val subtitle = state.lastCheckTime.value()?.let { lastCheckTime ->
                stringResource(
                    id = R.string.security_center_dark_web_monitor_top_bar_subtitle,
                    lastCheckTime
                )
            }
            PassExtendedTopBar(
                title = stringResource(R.string.security_center_dark_web_monitor_top_bar_title),
                subtitle = subtitle,
                onUpClick = { onEvent(DarkWebUiEvent.OnUpClick) },
                titleIcon = {
                    SecurityCenterDarkWebHelpIcon(
                        iconTint = PassTheme.colors.textNorm,
                        onClick = {
                            DarkWebUiEvent.HelpClick(
                                titleResId = R.string.dark_web_help_dialog_title,
                                textResId = R.string.dark_web_help_dialog_subtitle
                            ).also(onEvent)
                        }
                    )
                },
                actions = { DarkWebStatusIndicator(status = state.darkWebStatus) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(top = Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            item {
                EmailBreachSection(
                    state = state.protonEmailState,
                    summaryType = DarkWebSummaryType.Proton,
                    onEvent = onEvent
                )
            }
            item {
                EmailBreachSection(
                    state = state.aliasEmailState,
                    summaryType = DarkWebSummaryType.Alias,
                    onEvent = onEvent
                )
            }
            item {
                CustomEmailsHeader(
                    modifier = Modifier.padding(
                        horizontal = Spacing.medium,
                        vertical = Spacing.small
                    ),
                    count = state.customEmailState.count(),
                    canAddCustomEmails = state.canAddCustomEmails,
                    onAddClick = { onEvent(DarkWebUiEvent.OnNewCustomEmailClick) }
                )
                CustomEmailsList(state = state, onEvent = onEvent)
            }
        }
    }
}
