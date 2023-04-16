package proton.android.pass.autofill.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import javax.inject.Inject

class TestAutofillManager @Inject constructor() : AutofillManager {

    private val statusFlow: MutableStateFlow<AutofillSupportedStatus> =
        MutableStateFlow(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService))

    override fun getAutofillStatus(): Flow<AutofillSupportedStatus> = statusFlow

    fun emitStatus(status: AutofillSupportedStatus) {
        statusFlow.tryEmit(status)
    }

    override fun openAutofillSelector() = Unit

    override fun disableAutofill() = Unit
}
