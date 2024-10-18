/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.alias.contacts.options.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.alias.contacts.AliasContactsNavigation
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnBlockContactClicked
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnCopyAddressClicked
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnDeleteContactClicked
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnSendEmailClicked
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnUnblockContactClicked
import proton.android.pass.features.alias.contacts.sendEmailIntent

@Composable
fun OptionsAliasContactBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: OptionsAliasViewModel = hiltViewModel(),
    onNavigate: (AliasContactsNavigation) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            OptionsAliasEvent.Close -> onNavigate(AliasContactsNavigation.CloseBottomSheet)
            OptionsAliasEvent.Idle -> {}
            is OptionsAliasEvent.SendEmail -> {
                sendEmailIntent(context, event.email)
                onNavigate(AliasContactsNavigation.CloseBottomSheet)
            }
        }
        viewModel.onConsumeEvent(state.event)
    }

    OptionsAliasContactContent(
        modifier = modifier,
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                OnBlockContactClicked -> viewModel.onBlockContact()
                OnCopyAddressClicked -> viewModel.onCopyEmail()
                OnDeleteContactClicked -> viewModel.onDeleteContact()
                OnSendEmailClicked -> viewModel.onSendEmail()
                OnUnblockContactClicked -> viewModel.onUnblockContact()
            }
        }
    )
}
