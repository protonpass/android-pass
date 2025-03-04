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

package proton.android.pass.features.report.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.account.selector.R
import proton.android.pass.features.report.presentation.AccountRowUIState
import proton.android.pass.features.report.presentation.AccountSelectorUIState
import me.proton.core.presentation.R as CoreR

@Composable
internal fun AccountSwitchContent(
    modifier: Modifier = Modifier,
    state: AccountSelectorUIState,
    onClick: (UserId) -> Unit
) {
    Column(
        modifier = modifier.bottomSheet()
    ) {
        BottomSheetTitle(
            title = stringResource(id = R.string.account_switch_title)
        )
        val list = state.accounts.map { account ->
            accountItem(
                accountRowUIState = account,
                onClick = onClick
            )
        }
        BottomSheetItemList(
            items = list.withDividers().toPersistentList()
        )
    }
}

private fun accountItem(accountRowUIState: AccountRowUIState, onClick: (UserId) -> Unit): BottomSheetItem =
    object : BottomSheetItem {

        override val title: @Composable () -> Unit = {
            BottomSheetItemTitle(
                text = accountRowUIState.email ?: stringResource(R.string.unknown_email)
            )
        }

        override val subtitle: @Composable (() -> Unit)? = null

        override val leftIcon: @Composable (() -> Unit)? = null

        override val endIcon: @Composable (() -> Unit)? = if (accountRowUIState.isPrimary) {
            {
                BottomSheetItemIcon(
                    iconId = CoreR.drawable.ic_proton_checkmark,
                    tint = PassTheme.colors.interactionNormMajor1
                )
            }
        } else {
            null
        }

        override val onClick: (() -> Unit) = { onClick(accountRowUIState.userId) }

        override val isDivider = false

    }
