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

package proton.android.pass.data.impl.usecases.capabilities

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveUserAccessData
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolder
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolderResult
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanCreateFolderImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeUserAccessData: ObserveUserAccessData,
    private val shareRepository: ShareRepository
) : CanCreateFolder {

    override fun invoke(shareId: ShareId): Flow<CanCreateFolderResult> = observeCurrentUser()
        .mapLatest { it.userId }
        .flatMapLatest { userId ->
            combine(
                observeUserAccessData(),
                shareRepository.observeById(userId, shareId)
            ) { userAccess, share ->
                CanCreateFolderResult(
                    roleAllows = share.canBeCreated,
                    planAllows = userAccess?.folderAllowed ?: false
                )
            }.catch { error ->
                if (error is ShareNotAvailableError) emit(CanCreateFolderResult(roleAllows = false, planAllows = false))
                else throw error
            }
        }

    override fun invoke(shareIds: List<ShareId>): Flow<Map<ShareId, CanCreateFolderResult>> {
        if (shareIds.isEmpty()) return flowOf(emptyMap())
        val shareIdSet = shareIds.toSet()
        return observeCurrentUser()
            .mapLatest { it.userId }
            .flatMapLatest { userId ->
                combine(
                    observeUserAccessData(),
                    shareRepository.observeAllShares(userId, includeHidden = false)
                ) { userAccess, shares ->
                    val folderAllowed = userAccess?.folderAllowed ?: false
                    shares
                        .filter { it.id in shareIdSet }
                        .associate { share ->
                            share.id to CanCreateFolderResult(
                                roleAllows = share.canBeCreated,
                                planAllows = folderAllowed
                            )
                        }
                }
            }
    }
}
