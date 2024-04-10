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

package proton.android.pass.features.security.center.customemail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.security.center.customemail.navigation.SecurityCenterCustomEmailNavDestination
import proton.android.pass.features.security.center.customemail.presentation.SecurityCenterCustomEmailEvent
import proton.android.pass.features.security.center.customemail.presentation.SecurityCenterCustomEmailViewModel

@Composable
fun SecurityCenterCustomEmailScreen(
    onNavigated: (SecurityCenterCustomEmailNavDestination) -> Unit,
    viewModel: SecurityCenterCustomEmailViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            is SecurityCenterCustomEmailEvent.OnEmailSent -> {
                onNavigated(
                    SecurityCenterCustomEmailNavDestination.VerifyEmail(
                        id = event.id,
                        email = event.email
                    )
                )
                viewModel.onEventConsumed()
            }

            SecurityCenterCustomEmailEvent.Idle -> {}
        }

    }
    SecurityCenterCustomEmailContent(
        emailAddress = viewModel.emailAddress,
        state = state,
        onUiEvent = { event ->
            when (event) {
                SecurityCenterCustomEmailUiEvent.Back -> onNavigated(
                    SecurityCenterCustomEmailNavDestination.Back
                )

                is SecurityCenterCustomEmailUiEvent.OnEmailChange ->
                    viewModel.onEmailChange(event.email)

                SecurityCenterCustomEmailUiEvent.AddCustomEmail -> viewModel.addCustomEmail()
            }
        }
    )
}
