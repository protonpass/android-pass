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

package proton.android.pass.featureitemdetail.impl.common

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
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.featureitemdetail.impl.R
import me.proton.core.presentation.compose.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Stable
sealed interface CannotPerformActionDialogType {

    @StringRes
    fun title(): Int

    @StringRes
    fun message(): Int

    fun showUpgrade(): Boolean

    @Stable
    object CannotEditBecauseNoPermissions : CannotPerformActionDialogType {
        @StringRes
        override fun title(): Int = R.string.item_detail_cannot_perform_action_edit_title
        override fun message(): Int = R.string.item_detail_cannot_perform_action_edit_no_permissions_message
        override fun showUpgrade() = false
    }

    @Stable
    object CannotEditBecauseNeedsUpgrade : CannotPerformActionDialogType {
        @StringRes
        override fun title(): Int = R.string.item_detail_cannot_perform_action_edit_title
        override fun message(): Int = R.string.item_detail_cannot_perform_action_edit_needs_upgrade_message
        override fun showUpgrade() = true
    }

    @Stable
    object CannotEditBecauseItemInTrash : CannotPerformActionDialogType {
        @StringRes
        override fun title(): Int = R.string.item_detail_cannot_perform_action_edit_title
        override fun message(): Int = R.string.item_detail_cannot_perform_action_edit_item_in_trash_message
        override fun showUpgrade() = false
    }

    @Stable
    object CannotShareBecauseNoPermissions : CannotPerformActionDialogType {
        @StringRes
        override fun title(): Int = R.string.item_detail_cannot_perform_action_share_no_permissions_title
        override fun message(): Int = R.string.item_detail_cannot_perform_action_share_no_permissions_message
        override fun showUpgrade() = false
    }
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
                .padding(top = 24.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProtonDialogTitle(title = stringResource(type.title()))
            Text(
                text = stringResource(id = type.message()),
                style = ProtonTheme.typography.defaultUnspecified
            )

            val (confirmText, cancelText) = if (type.showUpgrade()) {
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
                    if (type.showUpgrade()) {
                        onUpgrade()
                    } else {
                        onClose()
                    }
                }
            )
        }
    }
}
