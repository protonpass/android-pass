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

package proton.android.pass.features.item.options.aliases.trash.dialogs.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.item.options.aliases.trash.dialogs.presentation.ItemOptionsAliasTrashDialogEvent
import proton.android.pass.features.item.options.aliases.trash.dialogs.presentation.ItemOptionsAliasTrashDialogViewModel
import proton.android.pass.features.item.options.shared.navigation.ItemOptionsNavDestination

@Composable
fun ItemOptionsAliasTrashDialog(
    onNavigated: (ItemOptionsNavDestination) -> Unit,
    viewModel: ItemOptionsAliasTrashDialogViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            ItemOptionsAliasTrashDialogEvent.Idle -> {}

            ItemOptionsAliasTrashDialogEvent.OnDisableError,
            ItemOptionsAliasTrashDialogEvent.OnDisableSuccess,
            ItemOptionsAliasTrashDialogEvent.OnTrashError -> {
                onNavigated(ItemOptionsNavDestination.CloseScreen)
            }

            ItemOptionsAliasTrashDialogEvent.OnTrashSuccess -> {
                onNavigated(ItemOptionsNavDestination.TrashItem)
            }
        }

        onConsumeEvent(state.event)
    }

    ItemOptionsAliasTrashDialogContent(
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                ItemOptionsAliasTrashDialogUiEvent.OnDismiss -> {
                    onNavigated(ItemOptionsNavDestination.CloseScreen)
                }

                ItemOptionsAliasTrashDialogUiEvent.OnDisable -> {
                    onDisableAlias()
                }

                is ItemOptionsAliasTrashDialogUiEvent.OnRemindMeChange -> {
                    onChangeRemindMe(isRemindMeEnabled = uiEvent.value)
                }

                ItemOptionsAliasTrashDialogUiEvent.OnTrash -> {
                    onTrashAlias()
                }
            }
        }
    )
}
