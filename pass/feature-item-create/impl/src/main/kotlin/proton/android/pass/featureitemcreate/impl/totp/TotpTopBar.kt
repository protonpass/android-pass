package proton.android.pass.featureitemcreate.impl.totp

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
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.topbar.TopBarLoading
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.android.pass.composecomponents.impl.topbar.iconbutton.CrossBackIconButton
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TotpTopBar(
    modifier: Modifier = Modifier,
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    isLoadingState: IsLoadingState,
    onUpClick: () -> Unit,
    onSubmit: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    ProtonTopAppBar(
        modifier = modifier,
        title = { TopBarTitleView(title = topBarTitle) },
        navigationIcon = { CrossBackIconButton(onUpClick = onUpClick) },
        actions = {
            IconButton(
                enabled = isLoadingState == IsLoadingState.NotLoading,
                onClick = {
                    keyboardController?.hide()
                    onSubmit()
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
