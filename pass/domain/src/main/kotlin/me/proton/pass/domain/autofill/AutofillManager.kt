package me.proton.pass.domain.autofill

import kotlinx.coroutines.flow.Flow

interface AutofillManager {
    fun getAutofillStatus(): Flow<AutofillSupportedStatus>
    fun openAutofillSelector()
    fun disableAutofill()
}
