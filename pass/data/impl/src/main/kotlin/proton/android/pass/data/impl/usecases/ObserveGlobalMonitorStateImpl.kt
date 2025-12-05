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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.api.usecases.MonitorState
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveGlobalMonitorState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveGlobalMonitorStateImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val userAccessDataRepository: UserAccessDataRepository
) : ObserveGlobalMonitorState {
    override fun invoke(): Flow<MonitorState> = observeCurrentUser()
        .flatMapLatest { user ->
            userAccessDataRepository.observe(user.userId)
                .map { userAccessData ->
                    MonitorState(
                        protonMonitorEnabled = userAccessData?.protonMonitorEnabled ?: false,
                        aliasMonitorEnabled = userAccessData?.aliasMonitorEnabled ?: false
                    )
                }
                .distinctUntilChanged()
        }
}
