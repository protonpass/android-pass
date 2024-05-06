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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsContent
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreEvent
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreState

@Composable
internal fun ItemHistoryRestoreContent(
    modifier: Modifier = Modifier,
    state: ItemHistoryRestoreState,
    onNavigated: (ItemHistoryNavDestination) -> Unit,
    onEvent: (ItemHistoryRestoreUiEvent) -> Unit
) {
    when (state) {
        ItemHistoryRestoreState.Initial -> {
            ItemHistoryRestoreLoading()
        }

        is ItemHistoryRestoreState.ItemDetails -> {
            ItemHistoryRestoreDetails(
                modifier = modifier,
                onNavigated = onNavigated,
                onEvent = onEvent,
                state = state
            )
        }
    }
}

@Composable
private fun ItemHistoryRestoreLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ItemHistoryRestoreDetails(
    modifier: Modifier = Modifier,
    onNavigated: (ItemHistoryNavDestination) -> Unit,
    onEvent: (ItemHistoryRestoreUiEvent) -> Unit,
    state: ItemHistoryRestoreState.ItemDetails
) = with(state) {
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isDialogLoading by rememberSaveable { mutableStateOf(false) }

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
        onEvent(ItemHistoryRestoreUiEvent.OnEventConsumed(event))
    }

    val itemColors = passItemColors(itemCategory = itemDetailState.itemCategory)
    val context = LocalContext.current

    PassItemDetailsContent(
        modifier = modifier,
        itemDetailState = itemDetailState,
        itemColors = itemColors,
        topBar = {
            ItemHistoryRestoreTopBar(
                colors = itemColors,
                onUpClick = { onNavigated(ItemHistoryNavDestination.Back) },
                onRestoreClick = { onEvent(ItemHistoryRestoreUiEvent.OnRestoreClick) }
            )
        },
        onEvent = {
            when (it) {
                is PassItemDetailsUiEvent.OnSectionClick -> onEvent(
                    ItemHistoryRestoreUiEvent.OnSectionClick(
                        section = it.section,
                        field = it.field

                    )
                )

                is PassItemDetailsUiEvent.OnHiddenSectionClick -> onEvent(
                    ItemHistoryRestoreUiEvent.OnHiddenSectionClick(
                        state = it.state,
                        field = it.field
                    )
                )

                is PassItemDetailsUiEvent.OnHiddenSectionToggle -> onEvent(
                    ItemHistoryRestoreUiEvent.OnHiddenSectionToggle(
                        state = it.state,
                        hiddenState = it.hiddenState,
                        field = it.field
                    )
                )

                is PassItemDetailsUiEvent.OnLinkClick -> {
                    BrowserUtils.openWebsite(context, it.link)
                }

                is PassItemDetailsUiEvent.OnPasskeyClick -> onEvent(
                    ItemHistoryRestoreUiEvent.OnPasskeyClick(
                        shareId = shareId,
                        itemId = itemId,
                        passkey = it.passkey
                    )
                )
            }
        }
    )

    ItemHistoryRestoreConfirmationDialog(
        isVisible = isDialogVisible,
        isLoading = isDialogLoading,
        onConfirm = {
            onEvent(ItemHistoryRestoreUiEvent.OnRestoreConfirmClick(itemDetailState.itemContents))
        },
        onDismiss = {
            onEvent(ItemHistoryRestoreUiEvent.OnRestoreCancelClick)
        },
        revisionTime = itemRevision.revisionTime
    )
}
