package proton.android.pass.account.fakes.devicemigration

import me.proton.core.devicemigration.domain.usecase.IsEasyDeviceMigrationAvailable
import me.proton.core.domain.entity.UserId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeIsEasyDeviceMigrationAvailable @Inject constructor() : IsEasyDeviceMigrationAvailable {
    override suspend fun invoke(userId: UserId?): Boolean = false
}
