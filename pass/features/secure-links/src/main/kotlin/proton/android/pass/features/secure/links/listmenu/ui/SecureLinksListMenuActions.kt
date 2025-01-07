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

package proton.android.pass.features.secure.links.listmenu.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.PassBottomSheetActionLoading
import proton.android.pass.features.secure.links.R
import me.proton.core.presentation.R as CoreR

internal fun copyLink(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(R.string.secure_links_shared_action_copy_link)) }

    override val subtitle: @Composable (() -> Unit)?
        get() = null

    override val leftIcon: @Composable (() -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_squares) }

    override val endIcon: @Composable (() -> Unit)
        get() = {}

    override val onClick: (() -> Unit)
        get() = { onClick() }

    override val isDivider: Boolean
        get() = false

}

internal fun removeLink(
    isActive: Boolean,
    action: BottomSheetItemAction,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = if (isActive) {
                    R.string.secure_links_shared_action_delete_link
                } else {
                    R.string.secure_links_shared_action_delete_link_inactive
                }.let { textResId -> stringResource(id = textResId) },
                color = PassTheme.colors.passwordInteractionNormMajor1
            )
        }

    override val subtitle: @Composable (() -> Unit)?
        get() = null

    override val leftIcon: @Composable (() -> Unit)
        get() = {
            BottomSheetItemIcon(
                iconId = CoreR.drawable.ic_proton_trash,
                tint = PassTheme.colors.passwordInteractionNormMajor1
            )
        }

    override val endIcon: @Composable (() -> Unit)
        get() = {
            if (action == BottomSheetItemAction.Remove) {
                PassBottomSheetActionLoading()
            }
        }

    override val onClick: (() -> Unit)
        get() = { onClick() }

    override val isDivider: Boolean
        get() = false

}

internal fun removeLinks(action: BottomSheetItemAction, onClick: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {

        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.secure_links_shared_action_delete_links_inactive),
                    color = PassTheme.colors.passwordInteractionNormMajor1
                )
            }

        override val subtitle: @Composable (() -> Unit)?
            get() = null

        override val leftIcon: @Composable (() -> Unit)
            get() = {
                BottomSheetItemIcon(
                    iconId = CoreR.drawable.ic_proton_trash,
                    tint = PassTheme.colors.passwordInteractionNormMajor1
                )
            }

        override val endIcon: @Composable (() -> Unit)
            get() = {
                if (action == BottomSheetItemAction.Remove) {
                    PassBottomSheetActionLoading()
                }
            }

        override val onClick: (() -> Unit)
            get() = { onClick() }

        override val isDivider: Boolean
            get() = false

    }
