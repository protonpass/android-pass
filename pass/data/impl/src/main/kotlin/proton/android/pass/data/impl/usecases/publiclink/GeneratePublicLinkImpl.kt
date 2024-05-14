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

package proton.android.pass.data.impl.usecases.publiclink

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.publiclink.GeneratePublicLink
import proton.android.pass.data.api.usecases.publiclink.PublicLinkOptions
import proton.android.pass.data.impl.repositories.PublicLinkRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class GeneratePublicLinkImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val repository: PublicLinkRepository
) : GeneratePublicLink {

    override suspend fun invoke(
        userId: UserId?,
        shareId: ShareId,
        itemId: ItemId,
        options: PublicLinkOptions
    ): String {
        val id = userId ?: run {
            observeCurrentUser().firstOrNull()?.userId
                ?: throw IllegalStateException("No user logged in")
        }

        return repository.generatePublicLink(id, shareId, itemId, options)
    }

}
