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

package proton.android.pass.features.report.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import proton.android.pass.data.api.usecases.items.ObserveCanCreateItems
import javax.inject.Inject

@HiltViewModel
class AccountSwitchViewModel @Inject constructor(
    userManager: UserManager,
    private val accountManager: AccountManager,
    private val observeCanCreateItems: ObserveCanCreateItems
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userReadyFlow = accountManager.getAccounts(AccountState.Ready)
        .flatMapLatest { accounts ->
            val flows = accounts.map { account ->
                userManager.observeUser(account.userId).filterNotNull()
            }
            combine(flows) { it }
        }

    private val accountSwitchEventFlow: MutableStateFlow<AccountSwitchEvent> =
        MutableStateFlow(AccountSwitchEvent.Idle)

    internal val stateFlow = combine(
        accountManager.getPrimaryAccount(),
        userReadyFlow,
        accountSwitchEventFlow
    ) { primary, users, event ->
        val accountRowUiStateList = users.map {
            AccountRowUIState(
                userId = it.userId,
                email = it.email,
                isPrimary = primary?.userId == it.userId
            )
        }.toPersistentList()
        AccountSelectorUIState(
            accounts = accountRowUiStateList,
            event = event
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AccountSelectorUIState.Loading
        )

    internal fun onAccountSelected(userId: UserId) {
        viewModelScope.launch {
            if (!observeCanCreateItems(userId).first()) {
                accountManager.setAsPrimary(userId)
                AccountSwitchEvent.CreateItem
            } else {
                AccountSwitchEvent.CannotCreateItem
            }.also { event ->
                accountSwitchEventFlow.update { event }
            }
        }
    }

    internal fun onEventConsumed(event: AccountSwitchEvent) {
        accountSwitchEventFlow.compareAndSet(event, AccountSwitchEvent.Idle)
    }

}
