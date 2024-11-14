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

package proton.android.pass.features.sharing.sharingpermissions.bottomsheet

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.bottomSheetDivider
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.sharingpermissions.SharingType
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SharingPermissionsBottomSheetContent(
    modifier: Modifier = Modifier,
    state: SharingPermissionsBottomSheetUiState,
    onPermissionClick: (SharingType) -> Unit,
    onDeleteUserClick: () -> Unit
) {
    val (titleText, checkedPermission) = when (state.mode) {
        is SharingPermissionsEditMode.All ->
            stringResource(R.string.sharing_edit_permissions_set_access_level_all_members) to null

        is SharingPermissionsEditMode.EditOne -> stringResource(
            R.string.sharing_edit_permissions_set_access_level_single_member,
            state.mode.email
        ) to state.mode.sharingType
    }

    Column(modifier = modifier.bottomSheet()) {
        val items = mutableListOf(
            titleText(titleText),
            setAdminPermission(
                checked = checkedPermission == SharingType.Admin,
                onClick = { onPermissionClick(SharingType.Admin) }
            ),
            setWritePermission(
                checked = checkedPermission == SharingType.Write,
                onClick = { onPermissionClick(SharingType.Write) }
            ),
            setReadPermission(
                checked = checkedPermission == SharingType.Read,
                onClick = { onPermissionClick(SharingType.Read) }
            )
        )

        if (state.displayRemove) {
            items.add(bottomSheetDivider())
            items.add(removeUserRow { onDeleteUserClick() })
        }

        BottomSheetItemList(
            items = items.toPersistentList()
        )
    }
}

private fun titleText(text: String): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                style = ProtonTheme.typography.headlineNorm.copy(textAlign = TextAlign.Center)
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)?
        get() = null
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: (() -> Unit)?
        get() = null
    override val isDivider = false
}

private fun setAdminPermission(checked: Boolean, onClick: () -> Unit): BottomSheetItem = permissionRow(
    title = R.string.sharing_can_manage,
    subtitle = R.string.sharing_can_manage_description,
    checked = checked,
    onClick = onClick
)

private fun setWritePermission(checked: Boolean, onClick: () -> Unit): BottomSheetItem = permissionRow(
    title = R.string.sharing_can_edit,
    subtitle = R.string.sharing_can_edit_description,
    checked = checked,
    onClick = onClick
)

private fun setReadPermission(checked: Boolean, onClick: () -> Unit): BottomSheetItem = permissionRow(
    title = R.string.sharing_can_view,
    subtitle = R.string.sharing_can_view_description,
    checked = checked,
    onClick = onClick
)

private fun permissionRow(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    checked: Boolean,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(id = title),
                color = PassTheme.colors.textNorm
            )
        }
    override val subtitle: (@Composable () -> Unit)
        get() = {
            BottomSheetItemSubtitle(
                text = stringResource(id = subtitle),
                maxLines = 2
            )
        }
    override val leftIcon: (@Composable () -> Unit)?
        get() = null
    override val endIcon: (@Composable () -> Unit)?
        get() = if (checked) {
            { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_checkmark) }
        } else {
            null
        }
    override val onClick: (() -> Unit)
        get() = onClick
    override val isDivider = false
}

private fun removeUserRow(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(id = R.string.sharing_edit_permissions_remove_user),
                color = PassTheme.colors.signalDanger
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = {
            BottomSheetItemIcon(
                iconId = CoreR.drawable.ic_proton_trash_cross,
                tint = PassTheme.colors.signalDanger
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: (() -> Unit)
        get() = onClick
    override val isDivider = false
}
