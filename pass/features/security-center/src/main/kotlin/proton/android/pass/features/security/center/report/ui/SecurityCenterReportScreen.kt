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

package proton.android.pass.features.security.center.report.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsType
import proton.android.pass.features.security.center.report.navigation.SecurityCenterReportDestination
import proton.android.pass.features.security.center.report.presentation.SecurityCenterReportEvent
import proton.android.pass.features.security.center.report.presentation.SecurityCenterReportViewModel

@Composable
fun SecurityCenterReportScreen(
    onNavigated: (SecurityCenterReportDestination) -> Unit,
    viewModel: SecurityCenterReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            SecurityCenterReportEvent.Close -> {
                onNavigated(SecurityCenterReportDestination.Back)
            }
            SecurityCenterReportEvent.Idle -> {}
        }

        viewModel.consumeEvent(state.event)
    }

    SecurityCenterReportContent(
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SecurityCenterReportUiEvent.Back -> onNavigated(
                    SecurityCenterReportDestination.Back
                )

                is SecurityCenterReportUiEvent.OnMenuClick -> {
                    val addressOptionsType = if (uiEvent.id is BreachEmailId.Custom) {
                        AddressOptionsType.RemoveCustomEmail
                    } else {
                        if (state.isBreachExcludedFromMonitoring) {
                            AddressOptionsType.EnableMonitoring
                        } else {
                            AddressOptionsType.DisableMonitoring
                        }
                    }
                    SecurityCenterReportDestination.OnMenuClick(
                        id = uiEvent.id,
                        addressOptionsType = addressOptionsType
                    ).also(onNavigated)
                }


                is SecurityCenterReportUiEvent.EmailBreachDetail -> onNavigated(
                    SecurityCenterReportDestination.EmailBreachDetail(uiEvent.id)
                )

                is SecurityCenterReportUiEvent.MarkAsResolvedClick ->
                    viewModel.resolveEmailBreach(uiEvent.id)

                is SecurityCenterReportUiEvent.OnItemClick -> {
                    SecurityCenterReportDestination.ItemDetail(
                        shareId = uiEvent.shareId,
                        itemId = uiEvent.itemId
                    ).also(onNavigated)
                }
            }
        }
    )
}
