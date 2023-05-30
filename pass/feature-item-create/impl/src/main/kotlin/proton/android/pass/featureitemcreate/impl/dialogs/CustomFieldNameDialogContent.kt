package proton.android.pass.featureitemcreate.impl.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun CustomFieldNameDialogContent(
    modifier: Modifier = Modifier,
    canConfirm: Boolean,
    value: String,
    onChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProtonDialogTitle(
                modifier = Modifier.padding(vertical = 16.dp),
                title = stringResource(R.string.custom_field_dialog_title)
            )

            Text(
                text = stringResource(R.string.custom_field_dialog_body),
                style = ProtonTheme.typography.defaultWeak,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .roundedContainerNorm()
                    .padding(16.dp),
            ) {
                ProtonTextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    value = value,
                    onChange = onChange,
                    placeholder = {
                        ProtonTextFieldPlaceHolder(
                            text = stringResource(R.string.custom_field_dialog_placeholder)
                        )
                    },
                    textStyle = ProtonTheme.typography.defaultNorm
                )
            }
        }

        DialogCancelConfirmSection(
            modifier = Modifier.padding(16.dp),
            color = PassTheme.colors.loginInteractionNormMajor1,
            disabledColor = ProtonTheme.colors.interactionDisabled,
            confirmEnabled = canConfirm,
            onDismiss = onCancel,
            onConfirm = onConfirm
        )
    }

    RequestFocusLaunchedEffect(focusRequester)
}
