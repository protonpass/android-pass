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

package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun SelectionModeTopBar(
    modifier: Modifier = Modifier,
    selectionState: SelectionTopBarState,
    onEvent: (HomeUiEvent) -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier.padding(vertical = 12.dp),
        title = {
            Text(
                text = selectionState.selectedItemCount.toString(),
                color = PassTheme.colors.textNorm
            )
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(HomeUiEvent.StopBulk) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = PassTheme.colors.textNorm
                )
            }
        },
        actions = {
            if (selectionState.isTrash) {
                TrashSelectionModeTopBar(selectionState = selectionState, onEvent = onEvent)
            } else {
                NonTrashSelectionModeTopBar(selectionState = selectionState, onEvent = onEvent)
            }
        }
    )
}

@Composable
private fun RowScope.NonTrashSelectionModeTopBar(
    selectionState: SelectionTopBarState,
    onEvent: (HomeUiEvent) -> Unit
) {
    if (selectionState.isPinningEnabled) {
        when (selectionState.pinningLoadingState) {
            IsLoadingState.NotLoading -> {
                IconButton(
                    enabled = selectionState.actionsEnabled,
                    onClick = {
                        val event = if (selectionState.areAllSelectedPinned) {
                            HomeUiEvent.UnpinItemsActionClick
                        } else {
                            HomeUiEvent.PinItemsActionClick
                        }
                        onEvent(event)
                    }
                ) {
                    val iconRes = if (selectionState.areAllSelectedPinned) {
                        CompR.drawable.ic_unpin_angled
                    } else {
                        CompR.drawable.ic_pin_angled
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = if (selectionState.actionsEnabled) {
                            PassTheme.colors.textNorm
                        } else {
                            PassTheme.colors.textDisabled
                        }
                    )
                }
            }

            IsLoadingState.Loading -> {
                Box(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
    IconButton(
        enabled = selectionState.actionsEnabled,
        onClick = { onEvent(HomeUiEvent.MoveItemsActionClick) }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_proton_folder_arrow_in),
            contentDescription = null,
            tint = if (selectionState.actionsEnabled) {
                PassTheme.colors.textNorm
            } else {
                PassTheme.colors.textDisabled
            }
        )
    }
    IconButton(
        enabled = selectionState.actionsEnabled,
        onClick = { onEvent(HomeUiEvent.MoveToTrashItemsActionClick) }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_proton_trash),
            contentDescription = null,
            tint = if (selectionState.actionsEnabled) {
                PassTheme.colors.textNorm
            } else {
                PassTheme.colors.textDisabled
            }
        )
    }
}

@Composable
private fun RowScope.TrashSelectionModeTopBar(
    selectionState: SelectionTopBarState,
    onEvent: (HomeUiEvent) -> Unit
) {
    IconButton(
        enabled = selectionState.actionsEnabled,
        onClick = { onEvent(HomeUiEvent.RestoreItemsActionClick) }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_proton_clock_rotate_left),
            contentDescription = null,
            tint = if (selectionState.actionsEnabled) {
                PassTheme.colors.textNorm
            } else {
                PassTheme.colors.textDisabled
            }
        )
    }
    IconButton(
        enabled = selectionState.actionsEnabled,
        onClick = { onEvent(HomeUiEvent.PermanentlyDeleteItemsActionClick) }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_proton_trash_cross),
            contentDescription = null,
            tint = if (selectionState.actionsEnabled) {
                PassTheme.colors.textNorm
            } else {
                PassTheme.colors.textDisabled
            }
        )
    }
}

@Preview
@Composable
fun SelectionModeTopBarPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SelectionModeTopBar(
                selectionState = SelectionTopBarState(
                    isTrash = input.second,
                    selectedItemCount = 2,
                    areAllSelectedPinned = false,
                    isPinningEnabled = true,
                    pinningLoadingState = IsLoadingState.NotLoading,
                    actionsEnabled = true
                ),
                onEvent = {}
            )
        }
    }
}
