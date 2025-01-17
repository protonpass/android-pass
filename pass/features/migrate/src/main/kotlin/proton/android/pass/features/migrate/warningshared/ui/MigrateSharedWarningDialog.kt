/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.migrate.warningshared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.migrate.MigrateNavigation
import proton.android.pass.features.migrate.warningshared.presentation.MigrateSharedWarningEvent
import proton.android.pass.features.migrate.warningshared.presentation.MigrateSharedWarningViewModel

@Composable
fun MigrateSharedWarningDialog(
    modifier: Modifier = Modifier,
    onNavigate: (MigrateNavigation) -> Unit,
    viewModel: MigrateSharedWarningViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            MigrateSharedWarningEvent.Idle -> Unit

            is MigrateSharedWarningEvent.OnMigrateItems -> {
                MigrateNavigation.VaultSelectionForItemsMigration(
                    filter = event.filter
                ).also(onNavigate)
            }

            is MigrateSharedWarningEvent.OnMigrateVault -> {
                MigrateNavigation.VaultSelectionForVaultMigration(
                    shareId = event.shareId
                ).also(onNavigate)
            }
        }

        onConsumeEvent(state.event)
    }

    MigrateSharedWarningContent(
        modifier = modifier,
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                MigrateSharedWarningUiEvent.OnCancelClicked,
                MigrateSharedWarningUiEvent.OnDismissed -> onNavigate(MigrateNavigation.Close)

                MigrateSharedWarningUiEvent.OnContinueClicked -> onMigrate()
            }
        }
    )
}
