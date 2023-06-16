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

package proton.android.pass.autofill.ui.bottomsheet.itemoptions

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import me.proton.core.presentation.R as CoreR

@Composable
fun AutofillItemOptionsBottomSheetContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onTrash: () -> Unit
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = listOf(
            moveToTrash(isLoading, onTrash)
        ).withDividers().toPersistentList()
    )
}

internal fun moveToTrash(
    isLoading: Boolean,
    onMoveToTrash: () -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                val color = if (isLoading) {
                    PassTheme.colors.textHint
                } else {
                    PassTheme.colors.textNorm
                }
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.bottomsheet_move_to_trash),
                    color = color
                )
            }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = {
                BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_trash)
            }
        override val endIcon: (@Composable () -> Unit)?
            get() = if (isLoading) {
                {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            } else null
        override val onClick: (() -> Unit)?
            get() = if (!isLoading) {
                { onMoveToTrash() }
            } else null
        override val isDivider = false
    }
