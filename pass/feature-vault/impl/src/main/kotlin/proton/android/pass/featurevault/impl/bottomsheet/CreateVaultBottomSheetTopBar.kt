package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.feature.vault.impl.R
import proton.android.pass.log.api.PassLogger

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateVaultBottomSheetTopBar(
    modifier: Modifier = Modifier,
    buttonText: String,
    isLoading: Boolean,
    isButtonEnabled: Boolean,
    onCloseClick: () -> Unit,
    onCreateClick: () -> Unit
) {

    PassLogger.d("CarlosTest", "IsLoading: $isLoading")

    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Circle(
            backgroundColor = PassTheme.colors.accentPurpleOpaque,
            onClick = onCloseClick
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross),
                contentDescription = stringResource(R.string.bottomsheet_close_icon_content_description),
                tint = PassTheme.colors.accentPurpleOpaque
            )
        }

        val buttonColor = if (isButtonEnabled) {
            PassTheme.colors.accentPurpleOpaque
        } else {
            PassTheme.colors.accentPurpleWeakest
        }

        LoadingCircleButton(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = buttonColor,
            isLoading = isLoading,
            buttonEnabled = isButtonEnabled,
            text = {
                Text(
                    text = buttonText,
                    color = PassTheme.colors.textInverted,
                    style = PassTypography.body3Regular
                )
            },
            onClick = {
                keyboardController?.hide()
                onCreateClick()
            }
        )
    }
}

@Preview
@Composable
fun CreateVaultBottomSheetTopBarPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CreateVaultBottomSheetTopBar(
                buttonText = stringResource(R.string.bottomsheet_create_vault_button),
                isLoading = input.second,
                isButtonEnabled = true,
                onCloseClick = {},
                onCreateClick = {}
            )
        }
    }
}
