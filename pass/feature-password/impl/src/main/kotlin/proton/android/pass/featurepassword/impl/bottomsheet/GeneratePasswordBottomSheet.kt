package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import proton.android.pass.featurepassword.impl.GeneratePasswordNavigation

@Suppress("CyclomaticComplexMethod", "ComplexMethod")
@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun GeneratePasswordBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (GeneratePasswordNavigation) -> Unit,
) {
    val viewModel = hiltViewModel<GeneratePasswordViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    GeneratePasswordBottomSheetContent(
        modifier = modifier,
        state = state,
        onEvent = {
            when (it) {
                is GeneratePasswordEvent.OnPasswordModeChange -> {
                    viewModel.onPasswordModeChange(it.mode)
                }
                GeneratePasswordEvent.OnPasswordModeChangeClick -> {
                    onNavigate(GeneratePasswordNavigation.OnSelectPasswordMode)
                }
                is GeneratePasswordEvent.OnRandomIncludeNumbersChange -> {
                    viewModel.onIncludeNumbersChange(it.value)
                }
                is GeneratePasswordEvent.OnRandomLengthChange -> {
                    viewModel.onLengthChange(it.length)
                }
                is GeneratePasswordEvent.OnRandomUseCapitalLettersChange -> {
                    viewModel.onCapitalLettersChange(it.value)
                }
                is GeneratePasswordEvent.OnRandomUseSpecialCharactersChange -> {
                    viewModel.onHasSpecialCharactersChange(it.value)
                }
                GeneratePasswordEvent.OnRegeneratePasswordClick -> {
                    viewModel.regenerate()
                }
                is GeneratePasswordEvent.OnWordsCapitalizeChange -> {
                    viewModel.onWordsCapitalizeChange(it.value)
                }
                is GeneratePasswordEvent.OnWordsIncludeNumbersChange -> {
                    viewModel.onWordsIncludeNumbersChange(it.value)
                }
                is GeneratePasswordEvent.OnWordsCountChange -> {
                    viewModel.onWordsCountChange(it.count)
                }
                GeneratePasswordEvent.OnWordsSeparatorClick -> {
                    onNavigate(GeneratePasswordNavigation.OnSelectWordSeparator)
                }
                is GeneratePasswordEvent.OnWordsSeparatorChange -> {
                    viewModel.onWordsSeparatorChange(it.separator)
                }
            }
        },
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
                                onNavigate(GeneratePasswordNavigation.DismissBottomsheet)
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
                        modifier = Modifier.fillMaxWidth(),
                        onCancel = { onNavigate(GeneratePasswordNavigation.DismissBottomsheet) },
                        onConfirm = {
                            viewModel.onConfirm()
                            onNavigate(GeneratePasswordNavigation.DismissBottomsheet)
                        }
                    )
                }
            }
        }
    )
}
