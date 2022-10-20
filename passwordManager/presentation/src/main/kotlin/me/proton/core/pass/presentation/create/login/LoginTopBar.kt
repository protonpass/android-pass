package me.proton.core.pass.presentation.create.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.domain.ShareId

@ExperimentalComposeUiApi
@Composable
internal fun LoginTopBar(
    modifier: Modifier = Modifier,
    uiState: CreateUpdateLoginUiState,
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    onUpClick: () -> Unit,
    onSubmit: (ShareId) -> Unit,
    onSnackbarMessage: (LoginSnackbarMessages) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    ProtonTopAppBar(
        modifier = modifier,
        title = { TopBarTitleView(topBarTitle) },
        navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
        actions = {
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    when (uiState.shareId) {
                        None -> onSnackbarMessage(LoginSnackbarMessages.EmptyShareIdError)
                        is Some -> onSubmit(uiState.shareId.value)
                    }
                },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text(
                    text = stringResource(topBarActionName),
                    color = ProtonTheme.colors.brandNorm,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500
                )
            }
        }
    )
}

