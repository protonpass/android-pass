package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.login.CustomFieldsState
import me.proton.core.presentation.R as CoreR

@Composable
fun EnabledCustomFieldsContent(
    modifier: Modifier = Modifier,
    state: CustomFieldsState.Enabled,
    canEdit: Boolean,
    onEvent: (CustomFieldEvent) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.customFields.forEachIndexed { idx, field ->
            CustomFieldEntry(
                entry = field,
                canEdit = canEdit,
                onValueChange = { onEvent(CustomFieldEvent.OnValueChange(value = it, index = idx)) },
                onOptionsClick = { onEvent(CustomFieldEvent.OnCustomFieldOptions(index = idx)) }
            )
        }

        TransparentTextButton(
            text = stringResource(R.string.create_login_add_custom_field_button),
            icon = CoreR.drawable.ic_proton_plus,
            iconContentDescription = stringResource(R.string.create_login_add_custom_field_button_content_description),
            color = PassTheme.colors.loginInteractionNormMajor2,
            onClick = { onEvent(CustomFieldEvent.AddCustomField) }
        )
    }
}
