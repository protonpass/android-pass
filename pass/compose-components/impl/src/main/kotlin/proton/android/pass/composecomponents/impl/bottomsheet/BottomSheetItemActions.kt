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

package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.R
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Stable
sealed interface BottomSheetItemAction {

    @Stable
    data object None : BottomSheetItemAction

    @Stable
    data object Pin : BottomSheetItemAction

    @Stable
    data object Unpin : BottomSheetItemAction

    @Stable
    data object History : BottomSheetItemAction

    @Stable
    data object MonitorExclude : BottomSheetItemAction

    @Stable
    data object MonitorInclude : BottomSheetItemAction

}

fun monitorExclude(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_monitor_exclude)) }

    override val subtitle: @Composable (() -> Unit)?
        get() = null

    override val leftIcon: @Composable (() -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_eye_slash) }

    override val endIcon: @Composable (() -> Unit)
        get() = {
            if (action is BottomSheetItemAction.MonitorExclude) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }

    override val onClick: (() -> Unit)
        get() = { onClick() }

    override val isDivider: Boolean
        get() = false

}

fun monitorInclude(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_monitor_include)) }

    override val subtitle: @Composable (() -> Unit)?
        get() = null

    override val leftIcon: @Composable (() -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_eye) }

    override val endIcon: @Composable (() -> Unit)
        get() = {
            if (action is BottomSheetItemAction.MonitorInclude) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }

    override val onClick: (() -> Unit)
        get() = { onClick() }

    override val isDivider: Boolean
        get() = false

}

fun pin(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_pin_item)) }

    override val subtitle: @Composable (() -> Unit)?
        get() = null

    override val leftIcon: @Composable (() -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_pin_angled) }

    override val endIcon: @Composable (() -> Unit)
        get() = {
            if (action is BottomSheetItemAction.Pin) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }

    override val onClick: (() -> Unit)
        get() = { onClick() }

    override val isDivider: Boolean
        get() = false

}

fun unpin(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_unpin_item)) }

    override val subtitle: @Composable (() -> Unit)?
        get() = null

    override val leftIcon: @Composable (() -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_unpin_angled) }

    override val endIcon: @Composable (() -> Unit)
        get() = {
            if (action is BottomSheetItemAction.Unpin) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }

    override val onClick: (() -> Unit)
        get() = { onClick() }

    override val isDivider: Boolean
        get() = false

}

fun viewHistory(isFreePlan: Boolean, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_view_item_history)) }

    override val subtitle: @Composable (() -> Unit)?
        get() = null

    override val leftIcon: @Composable (() -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_clock_rotate_left) }

    override val endIcon: @Composable (() -> Unit)
        get() = {
            if (isFreePlan) {
                Image(
                    painter = painterResource(id = CompR.drawable.ic_pass_plus),
                    contentDescription = null
                )
            }
        }

    override val onClick: (() -> Unit)
        get() = { onClick() }

    override val isDivider: Boolean
        get() = false

}
