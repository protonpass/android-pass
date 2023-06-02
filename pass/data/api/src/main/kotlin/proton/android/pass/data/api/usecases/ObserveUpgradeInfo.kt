package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.pass.domain.Plan
import proton.pass.domain.PlanLimit

interface ObserveUpgradeInfo {
    operator fun invoke(forceRefresh: Boolean = false): Flow<UpgradeInfo>
}

data class UpgradeInfo(
    val isUpgradeAvailable: Boolean,
    val plan: Plan,
    val totalVaults: Int,
    val totalAlias: Int,
    val totalTotp: Int,
) {
    fun hasReachedVaultLimit() = hasReachedLimit(plan.vaultLimit, totalVaults)
    fun hasReachedAliasLimit() = hasReachedLimit(plan.aliasLimit, totalAlias)
    fun hasReachedTotpLimit() = hasReachedLimit(plan.totpLimit, totalTotp)

    private fun hasReachedLimit(planLimit: PlanLimit, count: Int): Boolean {
        if (!isUpgradeAvailable) return false

        return when (planLimit) {
            PlanLimit.Unlimited -> false
            is PlanLimit.Limited -> count >= planLimit.limit
        }
    }
}
