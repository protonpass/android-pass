package proton.android.pass.autofill.api

sealed interface AutofillSupportedStatus {
    object Unsupported : AutofillSupportedStatus
    data class Supported(val status: AutofillStatus) : AutofillSupportedStatus
}

sealed interface AutofillStatus {
    object Disabled : AutofillStatus
    object EnabledByOtherService : AutofillStatus
    object EnabledByOurService : AutofillStatus

    fun value(): Boolean = when (this) {
        Disabled -> false
        EnabledByOurService -> true
        EnabledByOtherService -> false
    }
}
