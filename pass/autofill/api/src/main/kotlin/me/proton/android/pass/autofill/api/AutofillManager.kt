package me.proton.android.pass.autofill.api

import kotlinx.coroutines.flow.Flow

interface AutofillManager {
    fun getAutofillStatus(): Flow<AutofillSupportedStatus>
    fun openAutofillSelector()
    fun disableAutofill()
}
