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

package proton.android.pass.initializer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import proton.android.pass.domain.UserId
import proton.android.pass.domain.account.Account
import proton.android.pass.domain.account.AccountState

fun Flow<Account>.justBecameReady(): Flow<Unit> {
    val initial = emptyMap<UserId, AccountState>() to false
    return scan(initial) { accumulator, currentAccount ->
        val (stateMap, _) = accumulator
        val updatedMap = stateMap + (currentAccount.userId to currentAccount.state)
        val justBecameReady = stateMap[currentAccount.userId] != AccountState.Ready &&
            currentAccount.state == AccountState.Ready
        updatedMap to justBecameReady
    }
        .filter { (_, justBecameReady) -> justBecameReady }
        .map { }
}
