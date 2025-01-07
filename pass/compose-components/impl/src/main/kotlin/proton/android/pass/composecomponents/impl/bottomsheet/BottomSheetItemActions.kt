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
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.R
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Stable
enum class BottomSheetItemAction {
    None, Pin, Unpin, History, Migrate, MonitorExclude, MonitorInclude, Remove, Restore, Trash, ResetHistory
}

fun copyNote(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_copy_item_note))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_squares)
    }

    override val endIcon: (@Composable () -> Unit)? = null

    override val onClick: () -> Unit = onClick

    override val isDivider = false

}

fun migrate(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_migrate_item))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_folder_arrow_in)
    }

    override val endIcon: (@Composable () -> Unit)? =
        if (action == BottomSheetItemAction.Migrate) {
            { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
        } else {
            null
        }

    override val onClick: () -> Unit = onClick

    override val isDivider = false

}

fun monitorExclude(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_monitor_exclude))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_eye_slash)
    }

    override val endIcon: @Composable (() -> Unit)? =
        if (action == BottomSheetItemAction.MonitorExclude) {
            { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
        } else {
            null
        }

    override val onClick: (() -> Unit) = onClick

    override val isDivider: Boolean = false

}

fun monitorInclude(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_monitor_include))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_eye)
    }

    override val endIcon: @Composable (() -> Unit)? =
        if (action == BottomSheetItemAction.MonitorInclude) {
            { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
        } else {
            null
        }

    override val onClick: (() -> Unit) = onClick

    override val isDivider: Boolean = false

}

fun pin(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_pin_item))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = R.drawable.ic_pin_angled)
    }

    override val endIcon: @Composable (() -> Unit)? = if (action == BottomSheetItemAction.Pin) {
        { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
    } else {
        null
    }

    override val onClick: (() -> Unit) = onClick

    override val isDivider: Boolean = false

}

fun unpin(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_unpin_item))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = R.drawable.ic_unpin_angled)
    }

    override val endIcon: @Composable (() -> Unit)? =
        if (action == BottomSheetItemAction.Unpin) {
            { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
        } else {
            null
        }

    override val onClick: (() -> Unit) = onClick

    override val isDivider: Boolean = false

}

fun viewHistory(isFreePlan: Boolean, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_view_item_history))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_clock_rotate_left)
    }

    override val endIcon: @Composable (() -> Unit)? = if (isFreePlan) {
        {
            Image(
                painter = painterResource(id = CompR.drawable.ic_pass_plus),
                contentDescription = null
            )
        }
    } else {
        null
    }

    override val onClick: (() -> Unit) = onClick

    override val isDivider: Boolean = false

}

fun trash(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_move_to_trash))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_trash)
    }

    override val endIcon: @Composable (() -> Unit)? =
        if (action == BottomSheetItemAction.Trash) {
            { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
        } else {
            null
        }

    override val onClick: (() -> Unit) = if (action == BottomSheetItemAction.Trash) {
        {}
    } else {
        onClick
    }

    override val isDivider = false

}

fun restore(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_restore))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_clock_rotate_left)
    }

    override val endIcon: @Composable (() -> Unit)? =
        if (action == BottomSheetItemAction.Restore) {
            { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
        } else {
            null
        }

    override val onClick: (() -> Unit) = onClick

    override val isDivider = false

}

fun delete(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(R.string.bottomsheet_delete_permanently),
            color = ProtonTheme.colors.notificationError
        )
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(
            iconId = CoreR.drawable.ic_proton_trash_cross,
            tint = ProtonTheme.colors.notificationError
        )
    }

    override val endIcon: (@Composable () -> Unit)? = null

    override val onClick: (() -> Unit) = onClick

    override val isDivider = false

}

fun leave(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_leave))
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_arrow_out_from_rectangle)
    }

    override val endIcon: (@Composable () -> Unit)? = null

    override val onClick: (() -> Unit) = onClick

    override val isDivider = false

}

fun resetHistory(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.bottomsheet_reset_history)) }
    override val subtitle: @Composable (() -> Unit)?
        get() = null
    override val leftIcon: @Composable (() -> Unit)
        get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_folder_arrow_in) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = { onClick() }
    override val isDivider = false
}

