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
import proton.pass.domain.Plan
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
                    else -> observeCanDisplayTotp(
                        userId = id,
                        shareId = shareId,
                        itemId = itemId,
                        plan = plan
                    )
                }
            }
    }

    private fun observeCanDisplayTotp(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        plan: Plan
    ): Flow<Boolean> = localItemDataSource.observeAllItemsWithTotp(userId = userId)
        .map { itemsWithTotp ->
            val allowedItems = itemsWithTotp.take(plan.totpLimit)
            allowedItems.any { it.shareId == shareId && it.itemId == itemId }
        }

    private suspend fun getUserId(userId: UserId?): UserId = if (userId == null) {
        accountManager.getPrimaryUserId().first()
            ?: throw IllegalStateException("UserId cannot be null")
    } else {
        userId
    }
}
