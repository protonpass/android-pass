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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isDialogLoading by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.event) {
        when (state.event) {
            SecurityCenterReportEvent.Close -> {
                onNavigated(SecurityCenterReportDestination.Back)
            }

            SecurityCenterReportEvent.Idle -> {}

            SecurityCenterReportEvent.OnEmailBreachesResolved -> {
                isDialogVisible = false
                isDialogLoading = false
            }

            SecurityCenterReportEvent.OnResolveEmailBreaches -> {
                isDialogVisible = true
            }

            SecurityCenterReportEvent.OnResolveEmailBreachesCancelled -> {
                isDialogVisible = false
                isDialogLoading = false
            }

            SecurityCenterReportEvent.OnResolveEmailBreachesConfirmed -> {
                isDialogLoading = true
            }
        }

        viewModel.consumeEvent(state.event)
    }

    SecurityCenterReportContent(
        state = state,
        isDialogVisible = isDialogVisible,
        isDialogLoading = isDialogLoading,
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
                    viewModel.onResolveEmailBreachesConfirmed(uiEvent.id)

                is SecurityCenterReportUiEvent.OnItemClick -> {
                    SecurityCenterReportDestination.ItemDetail(
                        shareId = uiEvent.shareId,
                        itemId = uiEvent.itemId
                    ).also(onNavigated)
                }

                SecurityCenterReportUiEvent.OnMarkEmailBreachesAsResolved -> {
                    viewModel.onResolveEmailBreaches()
                }

                SecurityCenterReportUiEvent.OnMarkEmailBreachesAsResolvedCancelled -> {
                    viewModel.onResolveEmailBreachesCancelled()
                }
            }
        }
    )
}
