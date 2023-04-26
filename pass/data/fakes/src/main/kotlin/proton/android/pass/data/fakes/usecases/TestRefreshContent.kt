package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.RefreshContent
import javax.inject.Inject

class TestRefreshContent @Inject constructor() : RefreshContent {

    private var result: Result<Unit> = Result.success(Unit)

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke(userId: UserId) = result.getOrThrow()
}
