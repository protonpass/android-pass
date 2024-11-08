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

package proton.android.pass.data.impl.usecases.securelink

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.securelink.ObserveHasAssociatedSecureLinks
import proton.android.pass.data.impl.repositories.SecureLinkRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class ObserveHasAssociatedSecureLinksImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val repository: SecureLinkRepository
) : ObserveHasAssociatedSecureLinks {

    override fun invoke(itemIds: Map<ShareId, List<ItemId>>): Flow<Boolean> = observeCurrentUser()
        .flatMapLatest { user ->
            repository.observeSecureLinks(user.userId).map { secureLinks ->
                secureLinks.any { secureLink ->
                    itemIds[secureLink.shareId]?.contains(secureLink.itemId) ?: false
                }
            }
        }
        .catch { error ->
            PassLogger.w(TAG, "There was an error while observing item associated secure links")
            PassLogger.w(TAG, error)
            emit(false)
        }

    private companion object {

        private const val TAG = "ObserveHasAssociatedSecureLinks"

    }

}
