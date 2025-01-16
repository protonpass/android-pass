/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils.openWebsite

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    onNavigate: (AccountNavigation) -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hasBeenSignedOut by viewModel.hasBeenSignedOut.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.RESUMED
    )
    LaunchedEffect(hasBeenSignedOut) {
        if (hasBeenSignedOut) {
            onNavigate(AccountNavigation.CloseScreen)
        }
    }

    val context = LocalContext.current
    AccountContent(
        modifier = modifier,
        state = state,
        onEvent = {
            when (it) {
                AccountContentEvent.Back -> onNavigate(AccountNavigation.CloseScreen)
                AccountContentEvent.PasswordManagement -> onNavigate(AccountNavigation.PasswordManagement)
                AccountContentEvent.RecoveryEmail -> onNavigate(AccountNavigation.RecoveryEmail)
                AccountContentEvent.Upgrade -> onNavigate(AccountNavigation.Upgrade)
                AccountContentEvent.DeleteAccount -> openWebsite(context, PASS_DELETE_ACCOUNT)
                AccountContentEvent.ManageAccount -> openWebsite(context, PASS_MANAGE_ACCOUNT)
                is AccountContentEvent.SignOut -> onNavigate(AccountNavigation.SignOut(it.userId))
                AccountContentEvent.Subscription -> onNavigate(AccountNavigation.Subscription)
                AccountContentEvent.SetExtraPassword -> onNavigate(AccountNavigation.SetExtraPassword)
                is AccountContentEvent.ExtraPasswordOptions ->
                    onNavigate(AccountNavigation.ExtraPasswordOptions(it.userId))

                AccountContentEvent.SecurityKeys -> onNavigate(AccountNavigation.SecurityKeys)
            }
        }
    )
}

private const val PASS_MANAGE_ACCOUNT = "https://account.proton.me/pass/account-password"
private const val PASS_DELETE_ACCOUNT = "https://account.proton.me/u/0/pass/account-password"

object AccountScreenTestTag {
    const val SCREEN = "AccountScreenTestTag"
}
