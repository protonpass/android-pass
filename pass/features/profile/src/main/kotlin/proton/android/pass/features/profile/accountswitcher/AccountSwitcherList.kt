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

package proton.android.pass.features.profile.accountswitcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.toSize
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.profile.AccountSwitchEvent
import proton.android.pass.features.profile.PlanInfo

@Composable
fun AccountSwitcherList(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    accountItemList: ImmutableList<AccountListItem>,
    onExpandedChange: (Boolean) -> Unit,
    onEvent: (AccountSwitchEvent) -> Unit
) {
    var rowSize by remember { mutableStateOf(Size.Zero) }
    val (primary, other) = accountItemList.partition { it is AccountListItem.Primary }

    Column(
        modifier = modifier
    ) {
        if (primary.isNotEmpty()) {
            AccountSwitcherRow(
                modifier = Modifier
                    .clickable { onExpandedChange(true) }
                    .onGloballyPositioned { coordinates ->
                        rowSize = coordinates.size.toSize()
                    },
                isCollapsed = true,
                extraOffset = false,
                accountListItem = primary.first(),
                onEvent = onEvent
            )
        }
        DropdownMenu(
            modifier = Modifier
                .background(PassTheme.colors.inputBackgroundNorm)
                .width(with(LocalDensity.current) { rowSize.width.toDp() }),
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            AccountSwitcherMenuContent(
                primary = primary.toPersistentList(),
                other = other.toPersistentList(),
                onEvent = onEvent,
                onExpandedChange = onExpandedChange
            )
        }
    }
}


@Preview
@Composable
fun AccountSwitcherListPreview(@PreviewParameter(ThemePreviewProvider::class) input: Boolean) {
    PassTheme(isDark = input) {
        Surface {
            AccountSwitcherList(
                isExpanded = false,
                accountItemList = persistentListOf(
                    AccountListItem.Primary(
                        AccountItem(
                            userId = UserId("1"),
                            name = "John Doe",
                            email = "",
                            state = AccountState.Ready,
                            initials = "J",
                            planInfo = PlanInfo.Hide
                        )
                    )
                ),
                onExpandedChange = {},
                onEvent = {}
            )
        }
    }
}
