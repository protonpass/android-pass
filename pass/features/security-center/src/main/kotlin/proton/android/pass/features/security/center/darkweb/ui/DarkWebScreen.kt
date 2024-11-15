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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.None
import proton.android.pass.features.security.center.darkweb.navigation.DarkWebMonitorNavDestination
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEvent
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebViewModel

@Composable
internal fun DarkWebScreen(
    modifier: Modifier = Modifier,
    onNavigate: (DarkWebMonitorNavDestination) -> Unit,
    viewModel: DarkWebViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            DarkWebEvent.Idle -> {}
            is DarkWebEvent.OnVerifyCustomEmail -> {
                onNavigate(
                    DarkWebMonitorNavDestination.VerifyEmail(
                        id = event.customEmailId,
                        email = event.email
                    )
                )
            }
        }
        viewModel.consumeEvent(state.event)
    }

    DarkWebContent(
        modifier = modifier,
        state = state,
        onEvent = { event ->
            when (event) {
                DarkWebUiEvent.OnUpClick -> onNavigate(DarkWebMonitorNavDestination.Back)

                is DarkWebUiEvent.OnAddCustomEmailClick -> {
                    viewModel.onAddSuggestion(event.email)
                }

                is DarkWebUiEvent.OnUnverifiedEmailOptionsClick -> onNavigate(
                    DarkWebMonitorNavDestination.UnverifiedEmailOptions(
                        id = event.id,
                        email = event.email
                    )
                )

                is DarkWebUiEvent.OnCustomEmailReportClick ->
                    onNavigate(
                        DarkWebMonitorNavDestination.CustomEmailReport(
                            id = event.id,
                            email = event.email
                        )
                    )

                DarkWebUiEvent.OnNewCustomEmailClick -> if (state.canAddCustomEmails) {
                    onNavigate(DarkWebMonitorNavDestination.AddEmail(None))
                } else {
                    onNavigate(DarkWebMonitorNavDestination.CannotAddCustomEmails)
                }

                DarkWebUiEvent.OnShowAllAliasEmailBreachClick ->
                    onNavigate(DarkWebMonitorNavDestination.AllAliasEmails)

                DarkWebUiEvent.OnShowAllProtonEmailBreachClick ->
                    onNavigate(DarkWebMonitorNavDestination.AllProtonEmails)

                is DarkWebUiEvent.OnShowAliasEmailReportClick ->
                    onNavigate(
                        DarkWebMonitorNavDestination.AliasEmailReport(
                            id = event.id,
                            email = event.email
                        )
                    )

                is DarkWebUiEvent.OnShowProtonEmailReportClick ->
                    onNavigate(
                        DarkWebMonitorNavDestination.ProtonEmailReport(
                            id = event.id,
                            email = event.email
                        )
                    )

                is DarkWebUiEvent.HelpClick -> DarkWebMonitorNavDestination.Help(
                    titleResId = event.titleResId,
                    textResId = event.textResId
                ).also(onNavigate)
            }
        }
    )
}
