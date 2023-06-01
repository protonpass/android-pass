package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.login.CustomFieldsState
import proton.android.pass.featureitemcreate.impl.login.LoginItemValidationErrors
import me.proton.core.presentation.R as CoreR

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EnabledCustomFieldsContent(
    modifier: Modifier = Modifier,
    state: CustomFieldsState.Enabled,
    validationErrors: ImmutableList<LoginItemValidationErrors.CustomFieldValidationError>,
    canEdit: Boolean,
    onEvent: (CustomFieldEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var addCustomFieldAction by remember { mutableStateOf(false) }
    val keyboardState by keyboardAsState()

    LaunchedEffect(keyboardState, addCustomFieldAction) {
        if (!keyboardState && addCustomFieldAction) {
            onEvent(CustomFieldEvent.AddCustomField)
            addCustomFieldAction = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.customFields.forEachIndexed { idx, field ->
            val entryModifier = when (val focusCustomField = state.focusCustomField) {
                is Some -> {
                    if (focusCustomField.value == idx) {
                        Modifier.focusRequester(focusRequester)
                    } else {
                        Modifier
                    }
                }

                None -> Modifier
            }
            val validationError = validationErrors.firstOrNull {
                when (it) {
                    is LoginItemValidationErrors.CustomFieldValidationError.EmptyField -> {
                        it.index == idx
                    }
                    is LoginItemValidationErrors.CustomFieldValidationError.InvalidTotp -> {
                        it.index == idx
                    }
                }
            }

            CustomFieldEntry(
                modifier = entryModifier,
                entry = field,
                validationError = validationError,
                canEdit = canEdit,
                onValueChange = {
                    onEvent(
                        CustomFieldEvent.OnValueChange(
                            value = it,
                            index = idx
                        )
                    )
                },
                onOptionsClick = {
                    onEvent(
                        CustomFieldEvent.OnCustomFieldOptions(
                            index = idx,
                            currentLabel = field.label
                        )
                    )
                }
            )
        }

        if (!state.isLimited) {
            TransparentTextButton(
                text = stringResource(R.string.create_login_add_custom_field_button),
                icon = CoreR.drawable.ic_proton_plus,
                iconContentDescription = stringResource(
                    R.string.create_login_add_custom_field_button_content_description
                ),
                color = PassTheme.colors.loginInteractionNormMajor2,
                onClick = {
                    focusManager.clearFocus(true)
                    addCustomFieldAction = true
                    keyboardController?.hide()
                }
            )
        }
    }

    RequestFocusLaunchedEffect(
        focusRequester = focusRequester,
        requestFocus = state.focusCustomField.isNotEmpty(),
        callback = { onEvent(CustomFieldEvent.FocusRequested) }
    )
}
