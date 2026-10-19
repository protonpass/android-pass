/*
 * Copyright (c) 2026 Proton AG
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
package proton.android.pass.data.impl.account

import me.proton.core.account.domain.entity.Account as CoreAccount
import me.proton.core.account.domain.entity.AccountState as CoreAccountState
import me.proton.core.user.domain.entity.Role as CoreRole
import me.proton.core.user.domain.entity.User as CoreUser
import me.proton.core.user.domain.entity.UserRecovery as CoreUserRecovery
import me.proton.core.user.domain.extension.getDisplayName
import me.proton.core.user.domain.extension.isSso
import proton.android.pass.domain.account.Account
import proton.android.pass.domain.account.AccountState
import proton.android.pass.domain.account.User
import proton.android.pass.domain.account.UserRecoveryState
import proton.android.pass.domain.account.UserRole

internal fun CoreAccountState.toDomain(): AccountState = when (this) {
    CoreAccountState.Ready -> AccountState.Ready
    CoreAccountState.Disabled -> AccountState.Disabled
    CoreAccountState.Removed -> AccountState.Removed
    else -> AccountState.Other
}

internal fun CoreAccount.toDomain(): Account = Account(
    userId = userId,
    username = username,
    state = state.toDomain()
)

internal fun CoreRole?.toDomain(): UserRole = when (this) {
    CoreRole.NoOrganization -> UserRole.NoOrganization
    CoreRole.OrganizationMember -> UserRole.OrganizationMember
    CoreRole.OrganizationAdmin -> UserRole.OrganizationAdmin
    else -> UserRole.Other
}

internal fun CoreUserRecovery.State.toDomain(): UserRecoveryState = when (this) {
    CoreUserRecovery.State.None -> UserRecoveryState.None
    CoreUserRecovery.State.Grace -> UserRecoveryState.Grace
    CoreUserRecovery.State.Cancelled -> UserRecoveryState.Cancelled
    CoreUserRecovery.State.Insecure -> UserRecoveryState.Insecure
    CoreUserRecovery.State.Expired -> UserRecoveryState.Expired
}

internal fun CoreUser.toDomain(): User = User(
    userId = userId,
    email = email,
    name = name,
    displayName = getDisplayName(),
    role = role.toDomain(),
    isSso = isSso(),
    recoveryState = recovery?.state?.enum?.toDomain()
)
