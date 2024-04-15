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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachCustomEmailId
import proton.android.pass.domain.breach.BreachEmail

interface BreachRepository {

    fun observeCustomEmails(userId: UserId): Flow<List<BreachCustomEmail>>

    suspend fun addCustomEmail(userId: UserId, email: String): BreachCustomEmail

    suspend fun verifyCustomEmail(
        userId: UserId,
        emailId: BreachCustomEmailId,
        code: String
    )

    fun observeBreachesForCustomEmail(userId: UserId, id: BreachCustomEmailId): Flow<List<BreachEmail>>

    fun observeBreachesForAlias(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<BreachEmail>>
}
