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

package proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.ui

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
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

internal fun copyToClipboard(text: String, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(text = text)
    }

    override val subtitle: (@Composable () -> Unit)? = null

    override val leftIcon: (@Composable () -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_squares)
    }

    override val endIcon: (@Composable () -> Unit)? = null

    override val onClick: (() -> Unit) = onClick

    override val isDivider = false
}

internal fun moveToTrash(isLoading: Boolean, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        if (isLoading) {
            PassTheme.colors.textHint
        } else {
            PassTheme.colors.textNorm
        }.also { color ->
            BottomSheetItemTitle(
                text = stringResource(id = CompR.string.bottomsheet_move_to_trash),
                color = color
            )
        }
    }

    override val subtitle: (@Composable () -> Unit)? = null

    override val leftIcon: (@Composable () -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_trash)
    }

    override val endIcon: (@Composable () -> Unit)? = if (isLoading) {
        { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
    } else null

    override val onClick: (() -> Unit)? = onClick.takeIf { !isLoading }

    override val isDivider = false

}
