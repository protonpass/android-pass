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

package proton.android.pass.features.sl.sync.mailboxes.options.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.mailboxes.options.presentation.SimpleLoginSyncMailboxOptionsAction
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

internal fun delete(action: SimpleLoginSyncMailboxOptionsAction, onClick: () -> Unit) = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(id = CompR.string.action_delete),
            color = PassTheme.colors.textNorm
        )
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_trash)
    }

    override val endIcon: @Composable (() -> Unit) = {
        if (action == SimpleLoginSyncMailboxOptionsAction.Delete) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        }
    }

    override val onClick: (() -> Unit) = {
        if (action == SimpleLoginSyncMailboxOptionsAction.None) {
            onClick()
        }
    }

    override val isDivider: Boolean = false

}

internal fun setAsDefault(action: SimpleLoginSyncMailboxOptionsAction, onClick: () -> Unit) = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(id = CompR.string.action_make_default),
            color = PassTheme.colors.textNorm
        )
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_star)
    }

    override val endIcon: @Composable (() -> Unit) = {
        if (action == SimpleLoginSyncMailboxOptionsAction.SetAsDefault) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        }
    }

    override val onClick: (() -> Unit) = {
        if (action == SimpleLoginSyncMailboxOptionsAction.None) {
            onClick()
        }
    }

    override val isDivider: Boolean = false

}

internal fun verify(onClick: () -> Unit) = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(id = CompR.string.action_verify),
            color = PassTheme.colors.textNorm
        )
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_checkmark_circle)
    }

    override val endIcon: @Composable (() -> Unit)? = null

    override val onClick: (() -> Unit) = onClick

    override val isDivider: Boolean = false

}

internal fun changeEmail(onClick: () -> Unit) = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(id = R.string.simple_login_sync_mailbox_change_email_option),
            color = PassTheme.colors.textNorm
        )
    }

    override val subtitle: @Composable (() -> Unit)? = null

    override val leftIcon: @Composable (() -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_pencil)
    }

    override val endIcon: @Composable (() -> Unit)? = null

    override val onClick: (() -> Unit) = onClick

    override val isDivider: Boolean = false

}
