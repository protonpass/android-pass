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

package proton.android.pass.featureitemcreate.impl.attachments.attachmentoptions.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.featureitemcreate.impl.R
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun AttachmentOptionsContent(modifier: Modifier = Modifier, onEvent: (AttachmentOptionsEvent) -> Unit) {
    val list = listOf(
        openFile { AttachmentOptionsEvent.Open },
        saveFile { AttachmentOptionsEvent.Save },
        renameFile { AttachmentOptionsEvent.Rename },
        deleteFile { AttachmentOptionsEvent.Delete }
    ).withDividers().toPersistentList()
    BottomSheetItemList(
        modifier = modifier,
        items = list
    )
}

private fun openFile(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(R.string.attachment_options_open)
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: @Composable () -> Unit
        get() = {
            BottomSheetItemIcon(
                iconId = CoreR.drawable.ic_proton_eye
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = onClick
    override val isDivider = false
}

private fun saveFile(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(R.string.attachment_options_save)
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: @Composable () -> Unit
        get() = {
            BottomSheetItemIcon(
                iconId = CoreR.drawable.ic_proton_arrow_down_to_square
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = onClick
    override val isDivider = false
}

private fun renameFile(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(R.string.attachment_options_rename)
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: @Composable () -> Unit
        get() = {
            BottomSheetItemIcon(
                iconId = CompR.drawable.ic_text_style
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = onClick
    override val isDivider = false
}

private fun deleteFile(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(R.string.attachment_options_delete)
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: @Composable () -> Unit
        get() = {
            BottomSheetItemIcon(
                iconId = CoreR.drawable.ic_proton_trash
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = onClick
    override val isDivider = false
}
