/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.alias.contacts.create.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.alias.contacts.AliasContactsNavigation
import proton.android.pass.features.alias.contacts.create.presentation.CreateAliasContactEvent
import proton.android.pass.features.alias.contacts.create.presentation.CreateAliasContactUIEvent
import proton.android.pass.features.alias.contacts.create.presentation.CreateAliasContactViewModel

@Composable
fun CreateAliasContactScreen(
    modifier: Modifier = Modifier,
    viewmodel: CreateAliasContactViewModel = hiltViewModel(),
    onNavigate: (AliasContactsNavigation) -> Unit
) {
    val state by viewmodel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            CreateAliasContactEvent.Idle -> {}
            CreateAliasContactEvent.OnContactCreated -> onNavigate(AliasContactsNavigation.CloseScreen)
        }
        viewmodel.onEventConsumed(state.event)
    }

    CreateAliasContactContent(
        modifier = modifier,
        email = viewmodel.email,
        state = state,
        onEvent = {
            when (it) {
                CreateAliasContactUIEvent.Back -> onNavigate(AliasContactsNavigation.CloseScreen)
                CreateAliasContactUIEvent.Create -> viewmodel.onCreate()
                is CreateAliasContactUIEvent.EmailChanged -> viewmodel.onEmailChanged(it.email)
            }
        }
    )
}
