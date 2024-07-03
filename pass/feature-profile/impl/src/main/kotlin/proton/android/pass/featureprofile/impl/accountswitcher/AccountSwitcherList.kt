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

package proton.android.pass.featureprofile.impl.accountswitcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.featureprofile.impl.AccountSwitchEvent
import proton.android.pass.featureprofile.impl.R

@Composable
fun AccountSwitcherList(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    accountItemList: ImmutableList<AccountItem>,
    onExpandedChange: (Boolean) -> Unit,
    onEvent: (AccountSwitchEvent) -> Unit
) {
    var rowSize by remember { mutableStateOf(Size.Zero) }
    val (primary, other) = accountItemList.partition { it.state == AccountState.Primary }

    Column(
        modifier = modifier
            .padding(horizontal = Spacing.medium)
            .roundedContainerNorm()
    ) {
        AccountSwitcherRow(
            modifier = Modifier
                .clickable { onExpandedChange(true) }
                .onGloballyPositioned { coordinates ->
                    rowSize = coordinates.size.toSize()
                },
            isCollapsed = true,
            accountItem = primary.first(),
            onEvent = onEvent
        )
        DropdownMenu(
            modifier = Modifier
                .background(PassTheme.colors.inputBackgroundNorm)
                .width(with(LocalDensity.current) { rowSize.width.toDp() }),
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            AccountSwitcherRow(
                modifier = Modifier.clickable { onExpandedChange(false) },
                accountItem = primary.first(),
                onEvent = onEvent
            )
            PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
            Spacer(modifier = Modifier.height(30.dp))
            Text.Body1Regular(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                text = stringResource(R.string.account_switcher_switch_to)
            )
            other.forEach { accountItem ->
                AccountSwitcherRow(
                    modifier = Modifier.clickable {
                        onEvent(AccountSwitchEvent.OnAccountSelected(accountItem.userId))
                    },
                    accountItem = accountItem,
                    onEvent = onEvent
                )
                PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
            }
            AddAccount(onClick = { onEvent(AccountSwitchEvent.OnAddAccount) })
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
                    AccountItem(
                        userId = UserId("1"),
                        name = "John Doe",
                        email = "john.doe@proton.me",
                        state = AccountState.Primary
                    ),
                    AccountItem(
                        userId = UserId("3"),
                        name = "Jane Doe",
                        email = "jane.doe@proton.me",
                        state = AccountState.Ready
                    )
                ),
                onExpandedChange = {},
                onEvent = {}
            )
        }
    }
}
