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

package proton.android.pass.features.itemdetail.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultUnspecified
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.features.itemdetail.R
import me.proton.core.presentation.compose.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Stable
sealed class CannotPerformActionDialogType(
    @StringRes internal val title: Int,
    @StringRes internal val message: Int,
    internal val showUpgrade: Boolean
) {

    @Stable
    data object CannotEditBecauseNoPermissions : CannotPerformActionDialogType(
        title = R.string.item_detail_cannot_perform_action_edit_title,
        message = R.string.item_detail_cannot_perform_action_edit_no_permissions_message,
        showUpgrade = false
    )

    @Stable
    data object CannotEditBecauseNeedsUpgrade : CannotPerformActionDialogType(
        title = R.string.item_detail_cannot_perform_action_edit_title,
        message = R.string.item_detail_cannot_perform_action_edit_needs_upgrade_message,
        showUpgrade = true
    )

    @Stable
    data object CannotEditBecauseItemInTrash : CannotPerformActionDialogType(
        title = R.string.item_detail_cannot_perform_action_edit_title,
        message = R.string.item_detail_cannot_perform_action_edit_item_in_trash_message,
        showUpgrade = false
    )

    @Stable
    data object CannotShareBecauseLimitReached : CannotPerformActionDialogType(
        title = R.string.item_detail_cannot_perform_action_share_no_permissions_title,
        message = R.string.item_detail_cannot_perform_action_share_limit_reached,
        showUpgrade = true
    )

    @Stable
    data object CannotShareBecauseNoPermissions : CannotPerformActionDialogType(
        title = R.string.item_detail_cannot_perform_action_share_no_permissions_title,
        message = R.string.item_detail_cannot_perform_action_share_no_permissions_message,
        showUpgrade = false
    )

    @Stable
    data object CannotShareBecauseItemInTrash : CannotPerformActionDialogType(
        title = R.string.item_detail_cannot_perform_action_share_no_permissions_title,
        message = R.string.item_detail_cannot_perform_action_share_item_in_trash_message,
        showUpgrade = false
    )
}

@Composable
fun CannotPerformActionDialog(
    modifier: Modifier = Modifier,
    type: CannotPerformActionDialogType,
    onClose: () -> Unit,
    onUpgrade: () -> Unit
) {
    NoPaddingDialog(
        modifier = modifier,
        onDismissRequest = onClose
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = Spacing.mediumSmall),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            ProtonDialogTitle(title = stringResource(type.title))

            Text(
                text = stringResource(id = type.message),
                style = ProtonTheme.typography.defaultUnspecified
            )

            val (confirmText, cancelText) = if (type.showUpgrade) {
                stringResource(CompR.string.action_upgrade_now) to
                    stringResource(CoreR.string.presentation_alert_cancel)
            } else {
                stringResource(CoreR.string.presentation_alert_ok) to ""
            }

            DialogCancelConfirmSection(
                color = PassTheme.colors.loginInteractionNormMajor1,
                confirmText = confirmText,
                cancelText = cancelText,
                onDismiss = onClose,
                onConfirm = {
                    if (type.showUpgrade) {
                        onUpgrade()
                    } else {
                        onClose()
                    }
                }
            )
        }
    }
}
