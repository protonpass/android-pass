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

package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.remote.RemoteDataSourceConstants.PAGE_SIZE
import proton.android.pass.data.impl.responses.ShareKeyResponse
import proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteShareKeyDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteShareKeyDataSource {

    override fun getShareKeys(userId: UserId, shareId: ShareId): Flow<List<ShareKeyResponse>> = flow {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke {
                var page = 0
                val shareKeys = mutableListOf<ShareKeyResponse>()

                while (true) {
                    val pageKeys = getShareKeys(shareId.id, page, PAGE_SIZE)
                    shareKeys.addAll(pageKeys.keys.keys)

                    if (pageKeys.keys.keys.size < PAGE_SIZE) {
                        break
                    } else {
                        page++
                    }
                }
                shareKeys
            }
            .valueOrThrow
        emit(res)
    }
}
