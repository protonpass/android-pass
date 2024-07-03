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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.featureprofile.impl.AccountSwitchEvent
import proton.android.pass.featureprofile.impl.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun AccountSwitcherRow(
    modifier: Modifier = Modifier,
    isCollapsed: Boolean = false,
    accountItem: AccountItem,
    onEvent: (AccountSwitchEvent) -> Unit
) {
    Row(
        modifier = modifier
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        CircleTextIcon(
            text = accountItem.name.take(1),
            backgroundColor = PassTheme.colors.interactionNormMinor1,
            textColor = PassTheme.colors.interactionNormMajor2,
            shape = PassTheme.shapes.squircleMediumShape
        )
        Column(modifier = Modifier.weight(1f)) {
            Text.Body1Regular(text = accountItem.name)
            accountItem.email?.let {
                Text.Body3Regular(
                    text = it,
                    color = PassTheme.colors.textWeak
                )
            }
        }
        if (isCollapsed) {
            Icon.Default(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                id = CompR.drawable.ic_chevron_tiny_down,
                tint = PassTheme.colors.textWeak
            )
        } else {
            var expanded by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { expanded = true }
                ) {
                    Icon.Default(
                        id = CompR.drawable.ic_three_dots_vertical_24,
                        tint = PassTheme.colors.textWeak
                    )
                }
                DropdownMenu(
                    modifier = Modifier.background(PassTheme.colors.inputBackgroundStrong),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onEvent(AccountSwitchEvent.OnManageAccount(accountItem.userId))
                        }
                    ) {
                        Text.Body1Regular(text = stringResource(R.string.account_switcher_manage_account))
                    }
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onEvent(AccountSwitchEvent.OnSignOut(accountItem.userId))
                        }
                    ) {
                        Text.Body1Regular(text = stringResource(R.string.account_switcher_sign_out))
                    }
                }
            }
        }
    }
}

data class AccountItem(
    val userId: UserId,
    val name: String,
    val email: String?,
    val state: AccountState,
    val initials: String
)

sealed class AccountListItem(open val accountItem: AccountItem) {
    data class Primary(override val accountItem: AccountItem) : AccountListItem(accountItem)
    data class Ready(override val accountItem: AccountItem) : AccountListItem(accountItem)
    data class Disabled(override val accountItem: AccountItem) : AccountListItem(accountItem)
}

@Preview
@Composable
fun AccountSwitcherRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AccountSwitcherRow(
                isCollapsed = false,
                accountItem = AccountItem(
                    userId = UserId("1"),
                    name = "Username",
                    email = "email@proton.me",
                    state = AccountState.Ready,
                    initials = "U"
                ),
                onEvent = {}
            )
        }
    }
}
