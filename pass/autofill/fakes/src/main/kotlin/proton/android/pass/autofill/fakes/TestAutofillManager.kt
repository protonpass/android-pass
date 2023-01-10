package proton.android.pass.autofill.fakes

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillSupportedStatus

class TestAutofillManager : AutofillManager {
    private val statusFlow = MutableSharedFlow<AutofillSupportedStatus>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override fun getAutofillStatus(): Flow<AutofillSupportedStatus> = statusFlow

    fun emitStatus(status: AutofillSupportedStatus) {
        statusFlow.tryEmit(status)
    }

    override fun openAutofillSelector() = Unit

    override fun disableAutofill() = Unit
}
