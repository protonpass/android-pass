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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.badge.CircledBadge
import proton.android.pass.composecomponents.impl.badge.OverlayBadge
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.profile.AccountSwitchEvent
import proton.android.pass.features.profile.PlanInfo
import proton.android.pass.features.profile.R
import proton.android.pass.features.profile.accountswitcher.MenuOption.ManageAccount
import proton.android.pass.features.profile.accountswitcher.MenuOption.Remove
import proton.android.pass.features.profile.accountswitcher.MenuOption.SignIn
import proton.android.pass.features.profile.accountswitcher.MenuOption.SignOut
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun AccountSwitcherRow(
    modifier: Modifier = Modifier,
    isCollapsed: Boolean = false,
    extraOffset: Boolean,
    accountListItem: AccountListItem,
    onEvent: (AccountSwitchEvent) -> Unit
) {
    Row(
        modifier = modifier.padding(vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        OverlayBadge(
            modifier = Modifier.padding(start = Spacing.medium),
            isShown = accountListItem.accountItem.planInfo != PlanInfo.Hide,
            badge = {
                when (accountListItem.accountItem.planInfo) {
                    is PlanInfo.Unlimited -> CircledBadge(
                        icon = CompR.drawable.account_unlimited_indicator,
                        iconColor = PassTheme.colors.inputBackgroundNorm,
                        backgroundColor = PassTheme.colors.noteInteractionNormMajor2
                    )

                    PlanInfo.Hide -> {
                        // No badge
                    }
                }
            }
        ) {
            CircleTextIcon(
                text = accountListItem.accountItem.initials,
                backgroundColor = PassTheme.colors.interactionNormMajor2,
                textColor = ProtonTheme.colors.textInverted,
                shape = PassTheme.shapes.squircleSmallShape
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            val isDisabled = accountListItem.accountItem.state == AccountState.Disabled
            Text.Body1Regular(
                text = accountListItem.accountItem.name,
                color = if (isDisabled) ProtonTheme.colors.textDisabled else ProtonTheme.colors.textNorm
            )
            accountListItem.accountItem.email?.takeIfNotBlank()?.let {
                Text.Body3Regular(
                    text = it,
                    color = if (isDisabled) ProtonTheme.colors.textDisabled else ProtonTheme.colors.textWeak
                )
            }
        }
        if (isCollapsed) {
            Icon.Default(
                modifier = Modifier.padding(end = Spacing.small),
                id = CompR.drawable.ic_chevron_tiny_down,
                tint = PassTheme.colors.textWeak
            )
        } else {
            var expanded by remember { mutableStateOf(false) }
            if (accountListItem is AccountListItem.Primary) {
                Icon.Default(
                    id = CompR.drawable.ic_checkmark,
                    tint = PassTheme.colors.interactionNormMajor2
                )
            }
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
                    // Offset to align dropdown with the row since it's not positioned correctly
                    offset = DpOffset(0.dp, if (extraOffset) 80.dp else 40.dp),
                    onDismissRequest = { expanded = false }
                ) {
                    val list = when (accountListItem) {
                        is AccountListItem.Primary,
                        is AccountListItem.Ready -> listOf(ManageAccount, SignOut, Remove)

                        is AccountListItem.Disabled -> listOf(SignIn, Remove)
                    }
                    list.forEach {
                        when (it) {
                            ManageAccount ->
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        onEvent(
                                            AccountSwitchEvent.OnManageAccount(
                                                userId = accountListItem.accountItem.userId,
                                                email = accountListItem.accountItem.email.orEmpty(),
                                                isPrimary = accountListItem is AccountListItem.Primary
                                            )
                                        )
                                    }
                                ) {
                                    Icon.Default(id = CoreR.drawable.ic_proton_cog_wheel)
                                    Spacer(modifier = Modifier.width(Spacing.small))
                                    Text.Body1Regular(text = stringResource(R.string.account_switcher_manage_account))
                                }

                            SignOut ->
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        onEvent(AccountSwitchEvent.OnSignOut(accountListItem.accountItem.userId))
                                    }
                                ) {
                                    Icon.Default(id = CoreR.drawable.ic_proton_arrow_out_from_rectangle)
                                    Spacer(modifier = Modifier.width(Spacing.small))
                                    Text.Body1Regular(text = stringResource(R.string.account_switcher_sign_out))
                                }

                            SignIn ->
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        onEvent(AccountSwitchEvent.OnSignIn(accountListItem.accountItem.userId))
                                    }
                                ) {
                                    Icon.Default(id = CoreR.drawable.ic_proton_arrow_in_to_rectangle)
                                    Spacer(modifier = Modifier.width(Spacing.small))
                                    Text.Body1Regular(text = stringResource(R.string.account_switcher_sign_in))
                                }

                            Remove ->
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        onEvent(AccountSwitchEvent.OnRemoveAccount(accountListItem.accountItem.userId))
                                    }
                                ) {
                                    Icon.Default(id = CoreR.drawable.ic_remove_account)
                                    Spacer(modifier = Modifier.width(Spacing.small))
                                    Text.Body1Regular(text = stringResource(R.string.account_switcher_remove))
                                }
                        }
                    }
                }
            }
        }
    }
}

private enum class MenuOption {
    ManageAccount,
    SignOut,
    SignIn,
    Remove
}

@Preview
@Composable
fun AccountSwitcherRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AccountSwitcherRow(
                isCollapsed = false,
                extraOffset = false,
                accountListItem = AccountListItem.Primary(
                    accountItem = AccountItem(
                        userId = UserId("1"),
                        name = "Username",
                        email = "email@proton.me",
                        state = AccountState.Ready,
                        initials = "U",
                        planInfo = PlanInfo.Hide
                    )
                ),
                onEvent = {}
            )
        }
    }
}
