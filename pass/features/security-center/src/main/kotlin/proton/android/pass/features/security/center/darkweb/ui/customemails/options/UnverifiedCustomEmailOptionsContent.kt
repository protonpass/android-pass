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

package proton.android.pass.features.security.center.darkweb.ui.customemails.options

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.presentation.customemails.UnverifiedCustomEmailOptionsLoadingState

@Composable
internal fun UnverifiedCustomEmailOptionsContent(
    modifier: Modifier = Modifier,
    loading: UnverifiedCustomEmailOptionsLoadingState,
    onEvent: (UnverifiedCustomEmailOptionsUiEvent) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier,
        items = listOf(
            verify(loading == UnverifiedCustomEmailOptionsLoadingState.Verify) {
                onEvent(UnverifiedCustomEmailOptionsUiEvent.VerifyCustomEmail)
            },
            remove(loading == UnverifiedCustomEmailOptionsLoadingState.Remove) {
                onEvent(UnverifiedCustomEmailOptionsUiEvent.RemoveCustomEmail)
            }
        ).withDividers().toPersistentList()
    )
}

private fun verify(loading: Boolean, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(id = R.string.security_center_dark_web_monitor_custom_email_option_verify)
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_check_circle_full) }
    override val endIcon: (@Composable () -> Unit)?
        get() = if (loading) {
            { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
        } else null
    override val onClick: () -> Unit
        get() = { onClick() }
    override val isDivider = false
}

private fun remove(loading: Boolean, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(id = R.string.security_center_dark_web_monitor_custom_email_option_remove)
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_cross_circle) }
    override val endIcon: (@Composable () -> Unit)?
        get() = if (loading) {
            { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
        } else null
    override val onClick: () -> Unit
        get() = { onClick() }
    override val isDivider = false
}
