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

package proton.android.pass.features.alias.contacts.detail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.alias.contacts.AliasContactsNavigation
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactEvent
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactUIEvent
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactViewModel
import proton.android.pass.features.alias.contacts.sendEmailIntent

@Composable
fun DetailAliasContactScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailAliasContactViewModel = hiltViewModel(),
    onNavigate: (AliasContactsNavigation) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.event) {
        when (val event = state.event) {
            is DetailAliasContactEvent.CreateItem -> {
                onNavigate(AliasContactsNavigation.CreateContact(event.shareId, event.itemId))
            }

            DetailAliasContactEvent.Idle -> {}
        }
        viewModel.onEventConsumed(state.event)
    }

    LaunchedEffect(state.hasShownAliasContactsOnboarding) {
        if (!state.hasShownAliasContactsOnboarding) {
            viewModel.onShowOnboarding()
            onNavigate(AliasContactsNavigation.OnBoardingContacts)
        }
    }

    DetailAliasContactContent(
        modifier = modifier,
        state = state,
        onEvent = {
            when (it) {
                DetailAliasContactUIEvent.Back -> onNavigate(AliasContactsNavigation.CloseScreen)
                DetailAliasContactUIEvent.CreateContact -> viewModel.onCreateItem()
                DetailAliasContactUIEvent.Help -> onNavigate(AliasContactsNavigation.OnBoardingContacts)
                DetailAliasContactUIEvent.LearnMore -> onNavigate(AliasContactsNavigation.OnBoardingContacts)
                is DetailAliasContactUIEvent.BlockContact -> viewModel.onBlockContact(it.contactId)
                is DetailAliasContactUIEvent.UnblockContact -> viewModel.onUnblockContact(it.contactId)
                is DetailAliasContactUIEvent.ContactOptions -> {
                    val shareId = state.shareId.value() ?: return@DetailAliasContactContent
                    val itemId = state.itemId.value() ?: return@DetailAliasContactContent
                    onNavigate(AliasContactsNavigation.ContactOptions(shareId, itemId, it.contactId))
                }
                is DetailAliasContactUIEvent.SendEmail -> sendEmailIntent(context, it.email)
                DetailAliasContactUIEvent.Upgrade -> onNavigate(AliasContactsNavigation.Upgrade)
            }
        }
    )
}

