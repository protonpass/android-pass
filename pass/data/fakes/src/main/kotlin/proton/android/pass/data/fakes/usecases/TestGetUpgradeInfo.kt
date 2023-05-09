package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import proton.android.pass.data.api.usecases.GetUpgradeInfo
import proton.android.pass.data.api.usecases.UpgradeInfo
import javax.inject.Inject

class TestGetUpgradeInfo @Inject constructor() : GetUpgradeInfo {

    private var upgradeInfo: MutableSharedFlow<UpgradeInfo> = MutableStateFlow(DEFAULT)

    fun setResult(value: UpgradeInfo) {
        upgradeInfo.tryEmit(value)
    }

    override fun invoke(): Flow<UpgradeInfo> = upgradeInfo

    companion object {
        val DEFAULT = UpgradeInfo(
            isUpgradeAvailable = false,
            totalVaults = 0,
            vaultLimit = 0,
            totalAlias = 0,
            aliasLimit = 0,
            totalTotp = 0,
            totpLimit = 0
        )
    }
}
