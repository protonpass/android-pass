/*
 * Copyright (c) 2023 Proton AG
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
import proton.android.pass.test.TestUtils
import kotlin.random.Random

object TestUserAccessData {

    fun random(): UserAccessData = UserAccessData(
        pendingInvites = Random.nextInt(0, 10),
        waitingNewUserInvites = Random.nextInt(0, 10),
        needsUpdate = Random.nextBoolean(),
        protonMonitorEnabled = Random.nextBoolean(),
        aliasMonitorEnabled = Random.nextBoolean(),
        isSimpleLoginSyncEnabled = Random.nextBoolean(),
        minVersionUpgrade = if (Random.nextBoolean()) "1.0.0" else null,
        simpleLoginSyncDefaultShareId = TestUtils.randomString(),
        simpleLoginSyncPendingAliasCount = Random.nextInt(0, 10),
        canManageSimpleLoginAliases = Random.nextBoolean(),
        storageAllowed = Random.nextBoolean(),
        storageUsed = Random.nextLong(0, 1_000_000),
        storageQuota = Random.nextLong(1_000_000, 10_000_000),
        storageMaxFileSize = Random.nextLong(100_000, 1_000_000)
    )
}
