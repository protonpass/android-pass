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

package proton.android.pass.featureitemdetail.impl.note

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.leave
import proton.android.pass.composecomponents.impl.bottomsheet.pin
import proton.android.pass.composecomponents.impl.bottomsheet.unpin
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.ThemedTopBarOptionsPreviewProvider
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsParameters
import proton.android.pass.featureitemdetail.impl.common.migrate
import proton.android.pass.featureitemdetail.impl.common.moveToTrash

@Composable
fun NoteTopBarOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    canMigrate: Boolean,
    canMoveToTrash: Boolean,
    canLeave: Boolean,
    isPinned: Boolean,
    onMigrate: () -> Unit,
    onMoveToTrash: () -> Unit,
    onCopyNote: () -> Unit = {},
    onPinned: () -> Unit,
    onUnpinned: () -> Unit,
    onLeave: () -> Unit
) {
    buildList {
        copyNote(onClick = onCopyNote).also(::add)

        if (canMigrate) {
            migrate(onClick = onMigrate).also(::add)
        }

        if (isPinned) {
            unpin(
                action = BottomSheetItemAction.None,
                onClick = onUnpinned
            ).also(::add)
        } else {
            pin(
                action = BottomSheetItemAction.None,
                onClick = onPinned
            ).also(::add)
        }

        if (canMoveToTrash) {
            moveToTrash(onClick = onMoveToTrash).also(::add)
        }

        if (canLeave) {
            leave(onClick = onLeave).also(::add)
        }
    }.also { items ->
        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = items.withDividers().toPersistentList()
        )
    }
}

private fun copyNote(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.note_detail_copy_note_to_clipboard)) }
    override val subtitle: @Composable (() -> Unit)?
        get() = null
    override val leftIcon: @Composable (() -> Unit)
        get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_squares) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = { onClick() }
    override val isDivider = false
}

@Preview
@Composable
fun NoteTopBarOptionsBSContentsPreview(
    @PreviewParameter(ThemedTopBarOptionsPreviewProvider::class) input: Pair<Boolean, TopBarOptionsParameters>
) {
    val (isDark, params) = input

    PassTheme(isDark = isDark) {
        Surface {
            NoteTopBarOptionsBottomSheetContents(
                canMigrate = params.canMigrate,
                canMoveToTrash = params.canMoveToTrash,
                isPinned = params.isPinned,
                canLeave = false,
                onMigrate = {},
                onMoveToTrash = {},
                onPinned = {},
                onUnpinned = {},
                onLeave = {}
            )
        }
    }
}
