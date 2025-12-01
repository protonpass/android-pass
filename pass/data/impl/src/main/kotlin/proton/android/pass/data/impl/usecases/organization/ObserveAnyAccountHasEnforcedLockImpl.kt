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

package proton.android.pass.data.impl.usecases.organization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.isOrganizationUser
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.OrganizationSettingsRepository
import proton.android.pass.data.api.usecases.organization.ObserveAnyAccountHasEnforcedLock
import proton.android.pass.domain.OrganizationSettings
import javax.inject.Inject

class ObserveAnyAccountHasEnforcedLockImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val repository: OrganizationSettingsRepository
) : ObserveAnyAccountHasEnforcedLock {

    override fun invoke(): Flow<Option<OrganizationSettings>> = accountManager.getAccounts(AccountState.Ready)
        .map { accounts -> accounts.map { it.userId } }
        .flatMapLatest { userIds ->
            if (userIds.isEmpty()) return@flatMapLatest flowOf(None)

            combine(userIds.map { id -> userManager.observeUser(id) }) { it.toList() }
                .flatMapLatest { users ->
                    val orgUsers = users.filterNotNull().filter { it.isOrganizationUser() }
                    if (orgUsers.isEmpty()) return@flatMapLatest flowOf(None)
                    combine(
                        orgUsers.map { user ->
                            repository.observe(user.userId)
                                .onEach {
                                    if (it == null) {
                                        runCatching { repository.refresh(user.userId) }
                                    }
                                }
                        }
                    ) { settingsList ->
                        settingsList
                            .firstOrNull { it?.isEnforced() == true }
                            .toOption()
                    }
                }
        }
        .filterNotNull()

}
