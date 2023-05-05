package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow

interface GetUpgradeInfo {
    operator fun invoke(): Flow<UpgradeInfo>
}

data class UpgradeInfo(
    val isUpgradeAvailable: Boolean,
    val totalVaults: Int,
    val vaultLimit: Int,
    val totalAlias: Int,
    val aliasLimit: Int,
    val totalTotp: Int,
    val totpLimit: Int
)
