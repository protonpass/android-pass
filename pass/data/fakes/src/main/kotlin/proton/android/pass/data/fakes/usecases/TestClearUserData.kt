package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ClearUserData
import javax.inject.Inject

class TestClearUserData @Inject constructor() : ClearUserData {

    private var result = Result.success(Unit)

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke(userId: UserId) {
        result.getOrThrow()
    }
}
