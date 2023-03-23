package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount
import javax.inject.Inject

class TestGetVaultWithItemCountById @Inject constructor() : GetVaultWithItemCountById {

    private val flow: MutableSharedFlow<VaultWithItemCount> = FlowUtils.testFlow()
    private var exceptionOption: Option<Exception> = None

    fun emitValue(value: VaultWithItemCount) {
        flow.tryEmit(value)
    }

    fun sendException(exception: Exception) {
        exceptionOption = exception.toOption()
    }

    override fun invoke(userId: UserId?, shareId: ShareId): Flow<VaultWithItemCount> =
        flow.onSubscription {
            when (val exception = exceptionOption) {
                None -> {}
                is Some -> throw exception.value
            }
        }
}
