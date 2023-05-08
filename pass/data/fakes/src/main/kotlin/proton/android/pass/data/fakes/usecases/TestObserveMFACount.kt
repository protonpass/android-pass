package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.ObserveMFACount
import javax.inject.Inject

class TestObserveMFACount @Inject constructor() : ObserveMFACount {
    private val observeMFAFlow = testFlow<Int>()

    override fun invoke(): Flow<Int> = observeMFAFlow
}
