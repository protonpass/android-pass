package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.pass.domain.Plan

interface GetUpgradeInfo {
    operator fun invoke(): Flow<UpgradeInfo>
}

data class UpgradeInfo(
    val isUpgradeAvailable: Boolean,
    val plan: Plan,
    val totalVaults: Int,
    val totalAlias: Int,
    val totalTotp: Int,
) {
    fun hasReachedVaultLimit() = isUpgradeAvailable && totalVaults >= plan.vaultLimit
    fun hasReachedAliasLimit() = isUpgradeAvailable && totalAlias >= plan.aliasLimit
    fun hasReachedTotpLimit() = isUpgradeAvailable && totalTotp >= plan.totpLimit
}
