/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.test.domain

import proton.android.pass.domain.UserAccessData
import proton.android.pass.test.StringTestFactory
import kotlin.random.Random

object UserAccessDataTestFactory {

    @Suppress("LongParameterList")
    fun create(
        pendingInvites: Int = 0,
        waitingNewUserInvites: Int = 0,
        needsUpdate: Boolean = false,
        protonMonitorEnabled: Boolean = false,
        aliasMonitorEnabled: Boolean = false,
        isSimpleLoginSyncEnabled: Boolean = false,
        minVersionUpgrade: String? = null,
        simpleLoginSyncDefaultShareId: String = "",
        simpleLoginSyncPendingAliasCount: Int = 0,
        canManageSimpleLoginAliases: Boolean = false,
        storageAllowed: Boolean = false,
        storageUsed: Long = 1,
        storageQuota: Long = 100,
        storageMaxFileSize: Long = 1_048_576,
        folderAllowed: Boolean = false
    ): UserAccessData = UserAccessData(
        pendingInvites = pendingInvites,
        waitingNewUserInvites = waitingNewUserInvites,
        needsUpdate = needsUpdate,
        protonMonitorEnabled = protonMonitorEnabled,
        aliasMonitorEnabled = aliasMonitorEnabled,
        isSimpleLoginSyncEnabled = isSimpleLoginSyncEnabled,
        minVersionUpgrade = minVersionUpgrade,
        simpleLoginSyncDefaultShareId = simpleLoginSyncDefaultShareId,
        simpleLoginSyncPendingAliasCount = simpleLoginSyncPendingAliasCount,
        canManageSimpleLoginAliases = canManageSimpleLoginAliases,
        storageAllowed = storageAllowed,
        storageUsed = storageUsed,
        storageQuota = storageQuota,
        storageMaxFileSize = storageMaxFileSize,
        folderAllowed = folderAllowed
    )

    fun random(): UserAccessData = UserAccessData(
        pendingInvites = Random.nextInt(0, 10),
        waitingNewUserInvites = Random.nextInt(0, 10),
        needsUpdate = Random.nextBoolean(),
        protonMonitorEnabled = Random.nextBoolean(),
        aliasMonitorEnabled = Random.nextBoolean(),
        isSimpleLoginSyncEnabled = Random.nextBoolean(),
        minVersionUpgrade = if (Random.nextBoolean()) "1.0.0" else null,
        simpleLoginSyncDefaultShareId = StringTestFactory.randomString(),
        simpleLoginSyncPendingAliasCount = Random.nextInt(0, 10),
        canManageSimpleLoginAliases = Random.nextBoolean(),
        storageAllowed = Random.nextBoolean(),
        storageUsed = Random.nextLong(0, 1_000_000),
        storageQuota = Random.nextLong(1_000_000, 10_000_000),
        storageMaxFileSize = Random.nextLong(100_000, 1_000_000),
        folderAllowed = Random.nextBoolean()
    )
}
