package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCanDisplayTotp @Inject constructor() : CanDisplayTotp {

    private val flow = MutableStateFlow(Result.success(true))

    fun sendValue(value: Result<Boolean>) {
        flow.tryEmit(value)
    }

    override fun invoke(userId: UserId?, shareId: ShareId, itemId: ItemId): Flow<Boolean> =
        flow.map { it.getOrThrow() }
}
