package proton.android.pass.account.fakes.devicemigration

import me.proton.core.devicemigration.domain.feature.IsEasyDeviceMigrationEnabled
import me.proton.core.domain.entity.UserId

class FakeIsEasyDeviceMigrationEnabled : IsEasyDeviceMigrationEnabled {
    override fun invoke(userId: UserId?): Boolean = false
}
