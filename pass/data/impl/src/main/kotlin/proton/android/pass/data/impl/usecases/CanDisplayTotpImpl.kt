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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.pass.domain.ItemId
import proton.pass.domain.PlanLimit
import proton.pass.domain.PlanType
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanDisplayTotpImpl @Inject constructor(
    private val getUserPlan: GetUserPlan,
    private val accountManager: AccountManager,
    private val localItemDataSource: LocalItemDataSource
) : CanDisplayTotp {

    override fun invoke(
        userId: UserId?,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<Boolean> = flow {
        emit(getUserId(userId))
    }.flatMapLatest { id ->
        getUserPlan(id)
            .flatMapLatest { plan ->
                when (plan.planType) {
                    is PlanType.Paid, is PlanType.Trial -> flowOf(true)
                    else -> {
                        when (val limit = plan.totpLimit) {
                            PlanLimit.Unlimited -> flowOf(true)
                            is PlanLimit.Limited -> {
                                observeCanDisplayTotp(
                                    userId = id,
                                    shareId = shareId,
                                    itemId = itemId,
                                    limit = limit.limit
                                )
                            }
                        }
                    }
                }
            }
    }

    private fun observeCanDisplayTotp(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        limit: Int
    ): Flow<Boolean> = localItemDataSource.observeAllItemsWithTotp(userId = userId)
        .map { itemsWithTotp ->
            val allowedItems = itemsWithTotp.take(limit)
            allowedItems.any { it.shareId == shareId && it.itemId == itemId }
        }

    private suspend fun getUserId(userId: UserId?): UserId = if (userId == null) {
        accountManager.getPrimaryUserId().first()
            ?: throw IllegalStateException("UserId cannot be null")
    } else {
        userId
    }
}
