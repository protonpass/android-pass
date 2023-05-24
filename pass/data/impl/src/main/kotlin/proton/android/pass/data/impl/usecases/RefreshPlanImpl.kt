package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.usecases.RefreshPlan
import proton.android.pass.data.impl.repositories.PlanRepository
import javax.inject.Inject

class RefreshPlanImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val planRepository: PlanRepository,
) : RefreshPlan {
    override suspend fun invoke() {
        val userId = requireNotNull(accountManager.getPrimaryUserId().firstOrNull())
        planRepository
            .sendUserAccessAndObservePlan(
                userId = userId,
                forceRefresh = true
            )
            .first()
    }
}
