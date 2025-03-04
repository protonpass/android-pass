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

package proton.android.pass.features.report.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.report.navigation.AccountSwitchNavigation
import proton.android.pass.features.report.presentation.AccountSwitchEvent
import proton.android.pass.features.report.presentation.AccountSwitchViewModel

@Composable
fun AccountSwitchBottomSheet(
    viewModel: AccountSwitchViewModel = hiltViewModel(),
    onNavigate: (AccountSwitchNavigation) -> Unit
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            AccountSwitchEvent.Idle -> Unit
            AccountSwitchEvent.CannotCreateItem -> onNavigate(AccountSwitchNavigation.CannotCreateItem)
            AccountSwitchEvent.CreateItem -> onNavigate(AccountSwitchNavigation.CreateItem)
        }

        viewModel.onEventConsumed(state.event)
    }

    AccountSwitchContent(
        state = state,
        onClick = viewModel::onAccountSelected
    )
}
