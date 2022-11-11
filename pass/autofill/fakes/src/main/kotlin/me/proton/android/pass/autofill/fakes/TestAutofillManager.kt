package me.proton.android.pass.autofill.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import me.proton.android.pass.autofill.api.AutofillManager
import me.proton.android.pass.autofill.api.AutofillSupportedStatus

class TestAutofillManager : AutofillManager {
    override fun getAutofillStatus(): Flow<AutofillSupportedStatus> = emptyFlow()

    override fun openAutofillSelector() = Unit

    override fun disableAutofill() = Unit
}
