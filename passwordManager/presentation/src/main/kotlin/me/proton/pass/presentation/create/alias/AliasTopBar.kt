package me.proton.pass.presentation.create.alias

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.uievents.IsButtonEnabled

@Composable
fun AliasTopBar(
    @StringRes topBarTitle: Int,
    onUpClick: () -> Unit,
    isButtonEnabled: IsButtonEnabled,
    shareId: Option<ShareId>,
    onEmitSnackbarMessage: (AliasSnackbarMessage) -> Unit,
    onSubmit: (ShareId) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val (buttonEnabled, buttonTextColor) = when (isButtonEnabled) {
        IsButtonEnabled.Enabled -> true to ProtonTheme.colors.brandNorm
        IsButtonEnabled.Disabled -> false to ProtonTheme.colors.interactionDisabled
    }

    ProtonTopAppBar(
        title = { TopBarTitleView(topBarTitle) },
        navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
        actions = {
            IconButton(
                enabled = buttonEnabled,
                onClick = {
                    keyboardController?.hide()
                    when (shareId) {
                        None -> onEmitSnackbarMessage(AliasSnackbarMessage.EmptyShareIdError)
                        is Some -> onSubmit(shareId.value)
                    }
                },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    color = buttonTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500
                )
            }
        }
    )
}
