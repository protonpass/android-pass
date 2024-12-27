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

package proton.android.pass.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.R.drawable.ic_unpin_angled
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SelectionModeTopBar(
    modifier: Modifier = Modifier,
    selectionState: SelectionTopBarState,
    onEvent: (HomeUiEvent) -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier.padding(vertical = Spacing.mediumSmall),
        title = {
            Text(
                text = selectionState.selectedItemCount.toString(),
                color = PassTheme.colors.textNorm
            )
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(HomeUiEvent.StopBulk) }) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_arrow_back),
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
private fun NonTrashSelectionModeTopBar(
    modifier: Modifier = Modifier,
    selectionState: SelectionTopBarState,
    onEvent: (HomeUiEvent) -> Unit
) {
    Row(modifier = modifier) {
        IconButton(
            enabled = selectionState.actionsEnabled,
            onClick = { onEvent(HomeUiEvent.MoveItemsActionClick) }
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_folder_arrow_in),
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
                painter = painterResource(CoreR.drawable.ic_proton_trash),
                contentDescription = null,
                tint = if (selectionState.actionsEnabled) {
                    PassTheme.colors.textNorm
                } else {
                    PassTheme.colors.textDisabled
                }
            )
        }

        var showMenu by remember { mutableStateOf(false) }
        ThreeDotsMenuButton(enabled = selectionState.actionsEnabled, onClick = { showMenu = true })
        DropdownMenu(
            modifier = Modifier.background(PassTheme.colors.inputBackgroundNorm),
            expanded = showMenu && selectionState.selectedItemCount != 0,
            onDismissRequest = { showMenu = false }
        ) {
            PinDropDownMenuItem(
                enabled = selectionState.actionsEnabled,
                pinningState = selectionState.pinningState,
                onEvent = onEvent
            )
            if (selectionState.aliasState.areAllSelectedAliases) {
                AliasDropDownMenuItem(
                    enabled = selectionState.actionsEnabled,
                    aliasState = selectionState.aliasState,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun AliasDropDownMenuItem(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    aliasState: AliasState,
    onEvent: (HomeUiEvent) -> Unit
) {
    DropdownMenuItem(
        modifier = modifier,
        enabled = enabled,
        onClick = {
            val event = if (aliasState.areAllSelectedDisabled) {
                HomeUiEvent.EnableAliasItemsActionClick
            } else {
                HomeUiEvent.DisableAliasItemsActionClick
            }
            onEvent(event)
        }
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            val (text, icon) = if (aliasState.areAllSelectedDisabled) {
                Pair(stringResource(R.string.bulk_action_enable_alias), CoreR.drawable.ic_proton_alias)
            } else {
                Pair(stringResource(R.string.bulk_action_disable_alias), CompR.drawable.ic_alias_slash)
            }
            Text.Body1Regular(modifier = Modifier.weight(1f), text = text)
            Spacer(modifier = Modifier.width(100.dp))
            if (aliasState.aliasLoadingState is IsLoadingState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon.Default(
                    modifier = Modifier.size(24.dp),
                    id = icon,
                    tint = if (enabled) {
                        PassTheme.colors.textNorm
                    } else {
                        PassTheme.colors.textDisabled
                    }
                )
            }
        }
    }
}

@Composable
private fun PinDropDownMenuItem(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    pinningState: PinningState,
    onEvent: (HomeUiEvent) -> Unit
) {
    DropdownMenuItem(
        modifier = modifier,
        enabled = enabled,
        onClick = {
            val event = if (pinningState.areAllSelectedPinned) {
                HomeUiEvent.UnpinItemsActionClick
            } else {
                HomeUiEvent.PinItemsActionClick
            }
            onEvent(event)
        }
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            val (text, icon) = if (pinningState.areAllSelectedPinned) {
                Pair(stringResource(R.string.bulk_action_unpin), ic_unpin_angled)
            } else {
                Pair(stringResource(R.string.bulk_action_pin), CompR.drawable.ic_pin_angled)
            }
            Text.Body1Regular(modifier = Modifier.weight(1f), text = text)
            Spacer(modifier = Modifier.width(100.dp))
            if (pinningState.pinningLoadingState is IsLoadingState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon.Default(
                    modifier = Modifier.size(24.dp),
                    id = icon,
                    tint = if (enabled) {
                        PassTheme.colors.textNorm
                    } else {
                        PassTheme.colors.textDisabled
                    }
                )
            }
        }
    }
}


@Composable
private fun TrashSelectionModeTopBar(
    modifier: Modifier = Modifier,
    selectionState: SelectionTopBarState,
    onEvent: (HomeUiEvent) -> Unit
) {
    Row(modifier = modifier) {
        IconButton(
            enabled = selectionState.actionsEnabled,
            onClick = { onEvent(HomeUiEvent.RestoreItemsActionClick) }
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_clock_rotate_left),
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
                painter = painterResource(CoreR.drawable.ic_proton_trash_cross),
                contentDescription = null,
                tint = if (selectionState.actionsEnabled) {
                    PassTheme.colors.textNorm
                } else {
                    PassTheme.colors.textDisabled
                }
            )
        }
    }
}

@Preview
@Composable
fun SelectionModeTopBarPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            SelectionModeTopBar(
                selectionState = SelectionTopBarState(
                    isTrash = input.second,
                    selectedItemCount = 2,
                    pinningState = PinningState(false, IsLoadingState.NotLoading),
                    aliasState = AliasState(
                        areAllSelectedAliases = false,
                        areAllSelectedDisabled = false,
                        aliasLoadingState = IsLoadingState.NotLoading
                    ),
                    actionsEnabled = true
                ),
                onEvent = {}
            )
        }
    }
}
