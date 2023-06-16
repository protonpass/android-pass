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

package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.bottomSheetDivider
import proton.android.pass.featurehome.impl.R
import me.proton.core.presentation.R as CoreR

@ExperimentalMaterialApi
@Composable
fun TrashAllBottomSheetContents(
    modifier: Modifier = Modifier,
    onEmptyTrash: () -> Unit,
    onRestoreAll: () -> Unit
) {
    Column(
        modifier = modifier.bottomSheet(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BottomSheetTitle(
            title = stringResource(id = R.string.bottomsheet_trash_all_items_title)
        )
        BottomSheetItemList(
            items = persistentListOf(
                restoreAll(onRestoreAll),
                bottomSheetDivider(),
                emptyTrash(onEmptyTrash)
            )
        )
    }
}

private fun restoreAll(
    onRestoreAll: () -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_restore_all_items)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_clock_rotate_left) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = onRestoreAll
    override val isDivider = false
}

private fun emptyTrash(
    onEmptyTrash: () -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(id = R.string.bottomsheet_empty_trash),
                color = ProtonTheme.colors.notificationError
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = {
            BottomSheetItemIcon(
                iconId = CoreR.drawable.ic_proton_trash_cross,
                tint = ProtonTheme.colors.notificationError
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = onEmptyTrash
    override val isDivider = false
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun TrashAllBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TrashAllBottomSheetContents(
                onEmptyTrash = {},
                onRestoreAll = {}
            )
        }
    }
}


