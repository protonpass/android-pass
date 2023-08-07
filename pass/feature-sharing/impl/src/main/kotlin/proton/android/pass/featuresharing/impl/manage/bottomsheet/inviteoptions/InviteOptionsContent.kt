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

package proton.android.pass.featuresharing.impl.manage.bottomsheet.inviteoptions

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.featuresharing.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun InviteOptionsContent(
    modifier: Modifier = Modifier,
    state: InviteOptionsUiState,
    onEvent: (InviteOptionsUiEvent) -> Unit
) {
    val enabled = state.loadingOption == null
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = listOf(
            resendInvite(
                enabled = enabled,
                loading = state.loadingOption == LoadingOption.ResendInvite,
            ) {
                onEvent(InviteOptionsUiEvent.ResendInvite)
            },
            cancelInvite(
                enabled = enabled,
                loading = state.loadingOption == LoadingOption.CancelInvite
            ) {
                onEvent(InviteOptionsUiEvent.CancelInvite)
            }
        ).withDividers().toPersistentList()
    )
}

private fun resendInvite(enabled: Boolean, loading: Boolean, onClick: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                val color = if (enabled) {
                    PassTheme.colors.textNorm
                } else {
                    PassTheme.colors.textWeak
                }
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.sharing_bottomsheet_resend_invite),
                    color = color
                )
            }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_paper_plane) }
        override val endIcon: (@Composable () -> Unit)?
            get() = if (loading) {
                { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
            } else {
                null
            }
        override val onClick: (() -> Unit)?
            get() = if (enabled) {
                onClick
            } else {
                null
            }
        override val isDivider = false
    }


private fun cancelInvite(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                val color = if (enabled) {
                    PassTheme.colors.textNorm
                } else {
                    PassTheme.colors.textWeak
                }
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.sharing_bottomsheet_cancel_invite),
                    color = color
                )
            }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_circle_slash) }
        override val endIcon: (@Composable () -> Unit)?
            get() = if (loading) {
                { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
            } else {
                null
            }
        override val onClick: (() -> Unit)?
            get() = if (enabled) {
                onClick
            } else {
                null
            }
        override val isDivider = false
    }

