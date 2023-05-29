package proton.android.pass.featureitemcreate.impl.dialogs

import javax.annotation.concurrent.Immutable

sealed interface CustomFieldEvent {
    object Close : CustomFieldEvent
    object Unknown : CustomFieldEvent
}

@Immutable
data class CustomFieldNameUiState(
    val value: String,
    val canConfirm: Boolean,
    val event: CustomFieldEvent
) {
    companion object {
        val Initial = CustomFieldNameUiState(
            value = "",
            canConfirm = false,
            event = CustomFieldEvent.Unknown
        )
    }
}
