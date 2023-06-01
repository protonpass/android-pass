package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.login.LoginItemValidationErrors
import proton.android.pass.featureitemcreate.impl.login.LoginCustomField
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.HiddenState

@Composable
fun TotpCustomFieldEntry(
    modifier: Modifier = Modifier,
    content: CustomFieldContent.Totp,
    validationError: LoginItemValidationErrors.CustomFieldValidationError?,
    canEdit: Boolean,
    onChange: (String) -> Unit,
    onFocusChange: (LoginCustomField, Boolean) -> Unit,
    onOptionsClick: () -> Unit,
    index: Int
) {
    val value = when (val state = content.value) {
        is HiddenState.Concealed -> ""
        is HiddenState.Revealed -> state.clearText
    }

    val (isError, errorMessage) = when (validationError) {
        is LoginItemValidationErrors.CustomFieldValidationError.EmptyField ->
            true to
                stringResource(R.string.field_cannot_be_empty)
        is LoginItemValidationErrors.CustomFieldValidationError.InvalidTotp ->
            true to
                stringResource(R.string.totp_create_login_field_invalid)
        null -> false to ""
    }

    ProtonTextField(
        modifier = modifier
            .roundedContainerNorm()
            .padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
        value = value,
        onChange = onChange,
        editable = canEdit,
        isError = isError,
        errorMessage = errorMessage,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm,
        onFocusChange = { onFocusChange(LoginCustomField.CustomFieldTOTP(index), it) },
        label = { ProtonTextFieldLabel(text = content.label, isError = isError) },
        placeholder = {
            ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.totp_create_login_field_placeholder))
        },
        leadingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_lock),
                contentDescription = stringResource(R.string.mfa_icon_content_description),
                tint = if (isError) {
                    PassTheme.colors.signalDanger
                } else {
                    ProtonTheme.colors.iconWeak
                }
            )
        },
        trailingIcon = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (value.isNotEmpty()) {
                    SmallCrossIconButton { onChange("") }
                }
                CustomFieldOptionsButton(onClick = onOptionsClick)
            }
        }
    )
}

@Preview
@Composable
fun TotpCustomFieldEntryPreview(
    @PreviewParameter(ThemeTotpCustomFieldInput::class) input: Pair<Boolean, TotpCustomFieldInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            TotpCustomFieldEntry(
                content = CustomFieldContent.Totp(
                    label = "label",
                    value = HiddenState.Revealed("", input.second.text)
                ),
                validationError = input.second.error,
                canEdit = input.second.isEnabled,
                index = 0,
                onChange = {},
                onFocusChange = { _, _ -> },
                onOptionsClick = {},
            )
        }
    }
}
