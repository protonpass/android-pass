package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.CustomFieldsState

@Composable
fun CustomFieldsContent(
    modifier: Modifier = Modifier,
    state: CustomFieldsState,
    canEdit: Boolean,
    onNavigate: (BaseLoginNavigation) -> Unit
) {
    when (state) {
        CustomFieldsState.Disabled, CustomFieldsState.NotInitialised -> {}
        is CustomFieldsState.Enabled -> EnabledCustomFieldsContent(
            modifier = modifier,
            state = state,
            canEdit = canEdit,
            onNavigate = onNavigate
        )
        CustomFieldsState.Limited -> LimitedCustomFieldsContent(
            modifier = modifier
        )
    }
}
