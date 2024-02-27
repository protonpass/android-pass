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

package proton.android.pass.features.item.history.restore.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsContent
import proton.android.pass.composecomponents.impl.utils.protonItemColors
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreEvent
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreState

@Composable
internal fun ItemHistoryRestoreContent(
    modifier: Modifier = Modifier,
    onNavigated: (ItemHistoryNavDestination) -> Unit,
    onEventConsumed: (ItemHistoryRestoreEvent) -> Unit,
    onRestoreClick: () -> Unit,
    onRestoreConfirmClick: (ItemContents) -> Unit,
    onRestoreCancelClick: () -> Unit,
    onSectionClick: (String) -> Unit,
    onHiddenSectionClick: (HiddenState) -> Unit,
    onHiddenSectionToggle: (Boolean, HiddenState) -> Unit,
    state: ItemHistoryRestoreState,
) {
    when (state) {
        ItemHistoryRestoreState.Initial -> {
            ItemHistoryRestoreLoading()
        }

        is ItemHistoryRestoreState.ItemDetails -> {
            ItemHistoryRestoreDetails(
                modifier = modifier,
                onNavigated = onNavigated,
                onEventConsumed = onEventConsumed,
                onRestoreClick = onRestoreClick,
                onRestoreConfirmClick = onRestoreConfirmClick,
                onRestoreCancelClick = onRestoreCancelClick,
                onSectionClick = onSectionClick,
                onHiddenSectionClick = onHiddenSectionClick,
                onHiddenSectionToggle = onHiddenSectionToggle,
                state = state,
            )
        }
    }
}

@Composable
private fun ItemHistoryRestoreLoading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ItemHistoryRestoreDetails(
    modifier: Modifier = Modifier,
    onNavigated: (ItemHistoryNavDestination) -> Unit,
    onEventConsumed: (ItemHistoryRestoreEvent) -> Unit,
    onRestoreClick: () -> Unit,
    onRestoreConfirmClick: (ItemContents) -> Unit,
    onRestoreCancelClick: () -> Unit,
    onSectionClick: (String) -> Unit,
    onHiddenSectionClick: (HiddenState) -> Unit,
    onHiddenSectionToggle: (Boolean, HiddenState) -> Unit,
    state: ItemHistoryRestoreState.ItemDetails,
) = with(state) {
    var isDialogVisible by remember { mutableStateOf(false) }
    var isDialogLoading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = event) {
        when (event) {
            ItemHistoryRestoreEvent.Idle -> {

            }

            ItemHistoryRestoreEvent.OnItemRestored -> {
                isDialogVisible = false
                isDialogLoading = false
                onNavigated(ItemHistoryNavDestination.Detail)
            }

            ItemHistoryRestoreEvent.OnRestoreItem -> {
                isDialogVisible = true
            }

            ItemHistoryRestoreEvent.OnRestoreItemCanceled -> {
                isDialogVisible = false
                isDialogLoading = false
            }

            ItemHistoryRestoreEvent.OnRestoreItemConfirmed -> {
                isDialogLoading = true
            }
        }
        onEventConsumed(event)
    }

    val itemColors = protonItemColors(itemCategory = itemDetailState.itemCategory)

    PassItemDetailsContent(
        modifier = modifier,
        itemDetailState = itemDetailState,
        itemColors = itemColors,
        topBar = {
            ItemHistoryRestoreTopBar(
                colors = itemColors,
                onUpClick = { onNavigated(ItemHistoryNavDestination.Back) },
                onRestoreClick = onRestoreClick,
            )
        },
        onSectionClick = onSectionClick,
        onHiddenSectionClick = onHiddenSectionClick,
        onHiddenSectionToggle = onHiddenSectionToggle,
    )

    ItemHistoryRestoreConfirmationDialog(
        isVisible = isDialogVisible,
        isLoading = isDialogLoading,
        onConfirm = { onRestoreConfirmClick(itemDetailState.itemContents) },
        onDismiss = onRestoreCancelClick,
        revisionTime = itemRevision.revisionTime,
    )
}
