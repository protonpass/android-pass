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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.profile.AccountSwitchEvent
import proton.android.pass.features.profile.PlanInfo
import proton.android.pass.features.profile.R

@Composable
fun AccountSwitcherMenuContent(
    modifier: Modifier = Modifier,
    primary: ImmutableList<AccountListItem>,
    other: ImmutableList<AccountListItem>,
    onEvent: (AccountSwitchEvent) -> Unit,
    onExpandedChange: (Boolean) -> Unit
) {
    Column(modifier) {
        if (primary.isNotEmpty()) {
            AccountSwitcherRow(
                modifier = Modifier.clickable { onExpandedChange(false) },
                accountListItem = primary.first(),
                extraOffset = false,
                onEvent = onEvent
            )
            PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
            Spacer(modifier = Modifier.height(30.dp))
        }
        if (other.isNotEmpty()) {
            Text.Body1Regular(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                text = stringResource(R.string.account_switcher_switch_to)
            )
        }
        other.forEach { accountListItem ->
            AccountSwitcherRow(
                modifier = Modifier.clickable {
                    onEvent(AccountSwitchEvent.OnAccountSelected(accountListItem.accountItem.userId))
                },
                extraOffset = true,
                accountListItem = accountListItem,
                onEvent = onEvent
            )
            PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
        }
        AddAccount(onClick = { onEvent(AccountSwitchEvent.OnAddAccount) })
    }
}

@Preview
@Composable
fun AccountSwitcherMenuContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AccountSwitcherMenuContent(
                primary = persistentListOf(
                    AccountListItem.Primary(
                        accountItem = AccountItem(
                            userId = UserId("1"),
                            state = AccountState.Ready,
                            name = "John Doe",
                            email = "john.doe@proton.me",
                            initials = "J",
                            planInfo = PlanInfo.Unlimited("")
                        )
                    )
                ),
                other = persistentListOf(
                    AccountListItem.Ready(
                        accountItem = AccountItem(
                            userId = UserId("2"),
                            state = AccountState.Ready,
                            name = "Jane Doe",
                            email = "jane.doe@proton.me",
                            initials = "J",
                            planInfo = PlanInfo.Trial
                        )
                    ),
                    AccountListItem.Disabled(
                        accountItem = AccountItem(
                            userId = UserId("3"),
                            state = AccountState.Disabled,
                            // another
                            name = "Charles Doe",
                            email = "charles.doe@proton.me",
                            initials = "C",
                            planInfo = PlanInfo.Hide
                        )
                    )
                ),
                onEvent = {},
                onExpandedChange = {}
            )
        }
    }
}

