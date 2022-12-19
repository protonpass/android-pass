package me.proton.pass.presentation.create.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.TopBarLoading
import me.proton.pass.presentation.settings.ThemedBooleanPreviewProvider
import me.proton.pass.presentation.uievents.IsLoadingState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LoginTopBar(
    modifier: Modifier = Modifier,
    shareId: ShareId?,
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    isLoadingState: IsLoadingState,
    onUpClick: () -> Unit,
    onSubmit: (ShareId) -> Unit,
    onSnackbarMessage: (LoginSnackbarMessages) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    ProtonTopAppBar(
        modifier = modifier,
        title = { TopBarTitleView(title = topBarTitle) },
        navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
        actions = {
            IconButton(
                enabled = isLoadingState == IsLoadingState.NotLoading,
                onClick = {
                    keyboardController?.hide()
                    if (shareId == null) {
                        onSnackbarMessage(LoginSnackbarMessages.EmptyShareIdError)
                    } else {
                        onSubmit(shareId)
                    }
                },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                when (isLoadingState) {
                    IsLoadingState.Loading -> {
                        TopBarLoading()
                    }
                    IsLoadingState.NotLoading -> {
                        Text(
                            text = stringResource(topBarActionName),
                            color = ProtonTheme.colors.brandNorm,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun LoginTopBarPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            LoginTopBar(
                shareId = null,
                topBarTitle = R.string.title_create_login,
                topBarActionName = R.string.action_save,
                isLoadingState = IsLoadingState.from(input.second),
                onUpClick = {},
                onSubmit = {},
                onSnackbarMessage = {}
            )
        }
    }
}
