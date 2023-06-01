package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.featureitemcreate.impl.login.CustomFieldsState
import proton.android.pass.featureitemcreate.impl.login.LoginItemValidationErrors

@Composable
fun CustomFieldsContent(
    modifier: Modifier = Modifier,
    state: CustomFieldsState,
    validationErrors: ImmutableList<LoginItemValidationErrors.CustomFieldValidationError>,
    canEdit: Boolean,
    onEvent: (CustomFieldEvent) -> Unit
) {
    when (state) {
        CustomFieldsState.Disabled, CustomFieldsState.NotInitialised -> {}
        is CustomFieldsState.Enabled -> EnabledCustomFieldsContent(
            modifier = modifier,
            state = state,
            validationErrors = validationErrors,
            canEdit = canEdit,
            onEvent = onEvent
        )
    }
}
