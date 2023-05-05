package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.GetUpgradeInfo
import proton.android.pass.data.api.usecases.UpgradeInfo
import javax.inject.Inject

class TestGetUpgradeInfo @Inject constructor() : GetUpgradeInfo {

    private var upgradeInfo: MutableSharedFlow<UpgradeInfo> = testFlow()

    fun setResult(value: UpgradeInfo) {
        upgradeInfo.tryEmit(value)
    }

    override fun invoke(): Flow<UpgradeInfo> = upgradeInfo
}
