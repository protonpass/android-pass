package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCanPerformPaidAction @Inject constructor() : CanPerformPaidAction {

    private var result: MutableSharedFlow<Boolean> = MutableStateFlow(true)

    fun setResult(value: Boolean) {
        result.tryEmit(value)
    }

    override fun invoke(): Flow<Boolean> = result

}
