package proton.android.pass.featureitemcreate.impl.login.customfields

import proton.android.pass.featureitemcreate.impl.login.LoginCustomField

sealed interface CustomFieldEvent {

    object AddCustomField : CustomFieldEvent
    object Upgrade : CustomFieldEvent
    data class OnValueChange(val value: String, val index: Int) : CustomFieldEvent
    data class OnCustomFieldOptions(val currentLabel: String, val index: Int) : CustomFieldEvent
    data class FocusRequested(
        val loginCustomField: LoginCustomField,
        val isFocused: Boolean
    ) : CustomFieldEvent
}
