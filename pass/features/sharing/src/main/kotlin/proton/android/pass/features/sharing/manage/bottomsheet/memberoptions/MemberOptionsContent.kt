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

package proton.android.pass.features.sharing.manage.bottomsheet.memberoptions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.sharing.R
import me.proton.core.presentation.R as CoreR

@Composable
fun MemberOptionsContent(
    modifier: Modifier = Modifier,
    state: MemberOptionsUiState,
    onEvent: (MemberOptionsUiEvent) -> Unit
) {
    val enabled = state.isLoading == IsLoadingState.NotLoading
    val itemList = mutableListOf(
        setAdminPermission(
            enabled = enabled,
            loading = state.loadingOption == LoadingOption.Admin,
            checked = state.memberRole == ShareRole.Admin,
            isRenameAdminToManagerEnabled = state.isRenameAdminToManagerEnabled
        ) {
            onEvent(MemberOptionsUiEvent.SetPermission(MemberPermissionLevel.Admin))
        },
        setWritePermission(
            enabled = enabled,
            loading = state.loadingOption == LoadingOption.Write,
            checked = state.memberRole == ShareRole.Write
        ) {
            onEvent(MemberOptionsUiEvent.SetPermission(MemberPermissionLevel.Write))
        },
        setReadPermission(
            enabled = enabled,
            loading = state.loadingOption == LoadingOption.Read,
            checked = state.memberRole == ShareRole.Read
        ) {
            onEvent(MemberOptionsUiEvent.SetPermission(MemberPermissionLevel.Read))
        }
    )

    when (state.transferOwnership) {
        TransferOwnershipState.Hide -> {}
        TransferOwnershipState.Disabled -> {
            itemList += transferOwnership(
                enabled = false,
                subtitle = stringResource(id = R.string.sharing_bottomsheet_transfer_ownership_disabled_subtitle)
            )
        }

        TransferOwnershipState.Enabled -> {
            itemList += transferOwnership(enabled = enabled) {
                onEvent(MemberOptionsUiEvent.TransferOwnership)
            }
        }
    }

    itemList += removeAccess(
        enabled = enabled,
        loading = state.loadingOption == LoadingOption.RemoveMember
    ) {
        onEvent(MemberOptionsUiEvent.RemoveMember)
    }

    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = itemList
            .withDividers()
            .toPersistentList()
    )
}

@Composable
private fun setAdminPermission(
    isRenameAdminToManagerEnabled: Boolean,
    enabled: Boolean,
    checked: Boolean,
    loading: Boolean,
    onClick: () -> Unit
): BottomSheetItem = permissionRow(
    title = if (isRenameAdminToManagerEnabled) {
        R.string.sharing_can_manage_V2
    } else {
        R.string.sharing_can_manage
    },
    subtitle = R.string.sharing_can_manage_description,
    icon = CoreR.drawable.ic_proton_key,
    enabled = enabled,
    checked = checked,
    loading = loading,
    onClick = onClick
)

@Composable
private fun setWritePermission(
    enabled: Boolean,
    checked: Boolean,
    loading: Boolean,
    onClick: () -> Unit
): BottomSheetItem = permissionRow(
    title = R.string.sharing_can_edit,
    subtitle = R.string.sharing_can_edit_description,
    icon = CoreR.drawable.ic_proton_pencil,
    enabled = enabled,
    checked = checked,
    loading = loading,
    onClick = onClick
)

@Composable
private fun setReadPermission(
    enabled: Boolean,
    checked: Boolean,
    loading: Boolean,
    onClick: () -> Unit
): BottomSheetItem = permissionRow(
    title = R.string.sharing_can_view,
    subtitle = R.string.sharing_can_view_description,
    icon = CoreR.drawable.ic_proton_eye,
    enabled = enabled,
    checked = checked,
    loading = loading,
    onClick = onClick
)

@Suppress("LongParameterList")
@Composable
private fun permissionRow(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    @DrawableRes icon: Int,
    enabled: Boolean,
    checked: Boolean,
    loading: Boolean,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {
    val color = if (enabled) {
        PassTheme.colors.textNorm
    } else {
        PassTheme.colors.textWeak
    }

    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(id = title),
                color = color
            )
        }

    override val subtitle: (@Composable () -> Unit)
        get() = {
            BottomSheetItemSubtitle(
                text = stringResource(id = subtitle),
                color = PassTheme.colors.textWeak,
                maxLines = 2
            )
        }


    override val leftIcon: @Composable () -> Unit = {
        BottomSheetItemIcon(
            iconId = icon,
            tint = color
        )
    }

    override val endIcon: (@Composable () -> Unit)?
        get() = if (loading) {
            { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
        } else if (checked) {
            {
                BottomSheetItemIcon(
                    iconId = CoreR.drawable.ic_proton_checkmark,
                    tint = PassTheme.colors.interactionNormMajor2
                )
            }
        } else {
            null
        }
    override val onClick: (() -> Unit)?
        get() = if (enabled && !checked) {
            onClick
        } else {
            null
        }
    override val isDivider = false
}

private fun transferOwnership(
    enabled: Boolean,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            val color = if (enabled) {
                PassTheme.colors.textNorm
            } else {
                PassTheme.colors.textWeak
            }
            BottomSheetItemTitle(
                text = stringResource(id = R.string.sharing_bottomsheet_transfer_ownership),
                color = color
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = if (subtitle != null) {
            {
                BottomSheetItemSubtitle(
                    text = subtitle,
                    maxLines = 1
                )
            }
        } else null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_shield_half_filled) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: (() -> Unit)?
        get() = if (enabled) {
            onClick
        } else {
            null
        }
    override val isDivider = false
}


private fun removeAccess(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            val color = if (enabled) {
                PassTheme.colors.textNorm
            } else {
                PassTheme.colors.textWeak
            }
            BottomSheetItemTitle(
                text = stringResource(id = R.string.sharing_bottomsheet_remove_access),
                color = color
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_circle_slash) }
    override val endIcon: (@Composable () -> Unit)?
        get() = if (loading) {
            { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
        } else {
            null
        }
    override val onClick: (() -> Unit)?
        get() = if (enabled) {
            onClick
        } else {
            null
        }
    override val isDivider = false
}

@Preview
@Composable
fun MemberOptionsContentPreview(
    @PreviewParameter(ThemeMemberOptionsPreviewProvider::class) input: Pair<Boolean, MemberOptionInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            MemberOptionsContent(
                state = MemberOptionsUiState(
                    memberRole = ShareRole.Admin,
                    loadingOption = input.second.loadingOption,
                    transferOwnership = input.second.showTransferOwnership,
                    isLoading = input.second.isLoading,
                    event = MemberOptionsEvent.Unknown,
                    isRenameAdminToManagerEnabled = true
                ),
                onEvent = {}
            )
        }
    }
}
