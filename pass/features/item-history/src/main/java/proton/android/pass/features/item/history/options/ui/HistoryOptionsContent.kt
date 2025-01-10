/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.item.history.options.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.features.item.history.R
import me.proton.core.presentation.R as CoreR

@Composable
fun HistoryOptionsContent(modifier: Modifier = Modifier, onReset: () -> Unit) {
    BottomSheetItemList(
        modifier = modifier,
        items = persistentListOf(resetHistory { onReset() })
    )
}

private fun resetHistory(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(R.string.reset_history_bottomsheet_option),
                color = PassTheme.colors.signalDanger
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: @Composable () -> Unit
        get() = {
            BottomSheetItemIcon(
                iconId = CoreR.drawable.ic_proton_arrow_up_and_left,
                tint = PassTheme.colors.signalDanger
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = onClick
    override val isDivider = false
}
