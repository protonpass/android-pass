package proton.android.pass.featureitemdetail.impl.login.customfield

sealed interface CustomFieldEvent {
    data class ToggleFieldVisibility(val index: Int) : CustomFieldEvent
    data class CopyValue(val index: Int) : CustomFieldEvent
    data class CopyValueContent(val content: String) : CustomFieldEvent
    object Upgrade : CustomFieldEvent
}
