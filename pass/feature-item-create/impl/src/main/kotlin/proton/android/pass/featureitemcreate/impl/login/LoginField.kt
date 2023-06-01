package proton.android.pass.featureitemcreate.impl.login


sealed interface LoginField {
    object Title : LoginField
    object Username : LoginField
    object Password : LoginField
    object PrimaryTotp : LoginField
}

sealed class LoginCustomField : LoginField {
    abstract val index: Int

    data class CustomFieldText(override val index: Int) : LoginCustomField()
    data class CustomFieldHidden(override val index: Int) : LoginCustomField()
    data class CustomFieldTOTP(override val index: Int) : LoginCustomField()
}
