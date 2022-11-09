package me.proton.pass.domain.autofill

sealed interface AutofillSupportedStatus {
    object Unsupported : AutofillSupportedStatus
    data class Supported(val status: AutofillStatus) : AutofillSupportedStatus
}

sealed interface AutofillStatus {
    object Disabled : AutofillStatus
    object EnabledByOtherService : AutofillStatus
    object EnabledByOurService : AutofillStatus
}
