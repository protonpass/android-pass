/*
 * Copyright (c) 2023-2025 Proton AG
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

package proton.android.pass.features.itemcreate.alias.mailboxes.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation
import proton.android.pass.features.itemcreate.alias.mailboxes.presentation.SelectMailboxesEvent
import proton.android.pass.features.itemcreate.alias.mailboxes.presentation.SelectMailboxesViewModel

@Composable
internal fun SelectMailboxesBottomsheet(
    modifier: Modifier = Modifier,
    viewModel: SelectMailboxesViewModel = hiltViewModel(),
    canAddMailbox: Boolean,
    onNavigate: (BaseAliasNavigation) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.event) {
        when (uiState.event) {
            SelectMailboxesEvent.AddMailbox ->
                onNavigate(BaseAliasNavigation.AddMailbox)

            SelectMailboxesEvent.Idle -> {}
        }
        viewModel.onConsumeEvent(uiState.event)
    }
    SelectMailboxesContent(
        modifier = modifier,
        state = uiState,
        canAddMailbox = canAddMailbox,
        onEvent = {
            when (it) {
                SelectMailboxUiEvent.AddMailbox ->
                    viewModel.dismissFeatureDiscoveryBanner(addMailbox = true)

                SelectMailboxUiEvent.DismissFeatureDiscoveryBanner ->
                    viewModel.dismissFeatureDiscoveryBanner()

                is SelectMailboxUiEvent.SelectMailbox ->
                    viewModel.toggleMailbox(it.aliasMailbox)
            }
        }
    )
}
