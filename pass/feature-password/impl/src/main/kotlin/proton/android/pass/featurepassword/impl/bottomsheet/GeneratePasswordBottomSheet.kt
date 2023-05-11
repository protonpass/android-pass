package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetCancelConfirm
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.featurepassword.R


@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun GeneratePasswordBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val viewModel = hiltViewModel<GeneratePasswordViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    GeneratePasswordBottomSheetContent(
        modifier = modifier,
        state = state,
        onLengthChange = { viewModel.onLengthChange(it) },
        onRegenerateClick = { viewModel.regenerate() },
        onHasSpecialCharactersChange = { viewModel.onHasSpecialCharactersChange(it) },
        buttonSection = {
            when (state.mode) {
                GeneratePasswordMode.CopyAndClose ->
                    @Composable
                    {
                        CircleButton(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(14.dp),
                            color = PassTheme.colors.loginInteractionNormMajor1,
                            elevation = ButtonDefaults.elevation(0.dp),
                            onClick = {
                                viewModel.onConfirm()
                                onDismiss()
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.generate_password_copy),
                                style = PassTypography.body3RegularInverted,
                                color = PassTheme.colors.textInvert
                            )
                        }
                    }

                GeneratePasswordMode.CancelConfirm -> @Composable {
                    BottomSheetCancelConfirm(
                        modifier = Modifier.padding(top = 40.dp),
                        onCancel = onDismiss,
                        onConfirm = {
                            viewModel.onConfirm()
                            onDismiss()
                        }
                    )
                }
            }
        }
    )
}
