package proton.android.pass.featureitemcreate.impl.login.customfields

sealed interface CustomFieldEvent {

    object AddCustomField : CustomFieldEvent
    object Upgrade : CustomFieldEvent
    data class OnValueChange(val value: String, val index: Int) : CustomFieldEvent
    data class OnCustomFieldOptions(val currentLabel: String, val index: Int) : CustomFieldEvent
    object FocusRequested : CustomFieldEvent

}
