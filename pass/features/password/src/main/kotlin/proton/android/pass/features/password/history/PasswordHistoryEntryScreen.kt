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

package proton.android.pass.features.password.history

import androidx.activity.compose.BackHandler
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue.Hidden
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.domain.PasswordHistoryEntryId
import proton.android.pass.features.password.history.composable.ClearHistoryBottomSheet
import proton.android.pass.features.password.history.composable.ClearOneItemBottomSheet
import proton.android.pass.features.password.history.composable.PasswordHistoryEntryContent
import proton.android.pass.features.password.history.model.PasswordHistoryUiState

@Composable
internal fun PassHistoryEntry(viewModel: PasswordHistoryEntryViewModel = hiltViewModel(), onBackClick: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PasswordHistoryEntryScreen(
        state = state,
        onBackClick = onBackClick,
        onHideItem = viewModel::onHideItem,
        onRevealItem = viewModel::onRevealItem,
        onClearHistory = viewModel::onClearHistory,
        onClearItem = viewModel::onClearItem,
        onCopyPassword = viewModel::onCopyPassword
    )
}

private enum class BottomSheetToShow {
    Idle,
    BottomSheetClearHistory,
    BottomSheetRemoveFromHistory
}

val PasswordHistoryEntryIdSaver = Saver<PasswordHistoryEntryId?, Long>(
    save = { it?.id },
    restore = { it.let(::PasswordHistoryEntryId) }
)

@Composable
internal fun PasswordHistoryEntryScreen(
    state: PasswordHistoryUiState,
    onBackClick: () -> Unit,
    onCopyPassword: (PasswordHistoryEntryId) -> Unit,
    onHideItem: (PasswordHistoryEntryId) -> Unit,
    onRevealItem: (PasswordHistoryEntryId) -> Unit,
    onClearHistory: () -> Unit,
    onClearItem: (PasswordHistoryEntryId) -> Unit
) {
    var bottomSheetToShow by rememberSaveable { mutableStateOf(BottomSheetToShow.Idle) }
    var passwordHistoryEntryIdClicked by rememberSaveable(stateSaver = PasswordHistoryEntryIdSaver) {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(Hidden)

    fun hideBottomSheet() {
        scope.launch {
            sheetState.hide()
            bottomSheetToShow = BottomSheetToShow.Idle
            passwordHistoryEntryIdClicked = null
        }
    }

    BackHandler {
        if (sheetState.isVisible) {
            hideBottomSheet()
        } else {
            onBackClick()
        }
    }

    ModalBottomSheetLayout(
        sheetContent = {
            when (bottomSheetToShow) {
                BottomSheetToShow.BottomSheetClearHistory -> {
                    ClearHistoryBottomSheet(
                        onClearHistory = {
                            onClearHistory()
                            hideBottomSheet()
                        }
                    )
                }

                BottomSheetToShow.BottomSheetRemoveFromHistory -> {
                    ClearOneItemBottomSheet(
                        onClearItem = {
                            passwordHistoryEntryIdClicked?.let { onClearItem(it) }
                            hideBottomSheet()
                        }
                    )
                }

                BottomSheetToShow.Idle -> {}
            }
        },
        sheetState = sheetState,
        content = {
            PasswordHistoryEntryContent(
                state = state,
                onBackClick = onBackClick,
                onHideItem = onHideItem,
                onRevealItem = onRevealItem,
                onCopyPassword = onCopyPassword,
                onMainThreeDotsMenuButtonClick = {
                    bottomSheetToShow = BottomSheetToShow.BottomSheetClearHistory
                    scope.launch {
                        sheetState.show()
                    }
                },
                onThreeDotsMenuButtonClick = { passwordHistoryEntryId ->
                    passwordHistoryEntryIdClicked = passwordHistoryEntryId
                    bottomSheetToShow = BottomSheetToShow.BottomSheetRemoveFromHistory
                    scope.launch {
                        sheetState.show()
                    }
                }
            )
        }
    )
}




