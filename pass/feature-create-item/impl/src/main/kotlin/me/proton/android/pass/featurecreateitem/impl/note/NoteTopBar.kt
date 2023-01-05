package me.proton.android.pass.featurecreateitem.impl.note

import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.composecomponents.impl.topbar.TopBarLoading
import me.proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import me.proton.android.pass.composecomponents.impl.topbar.icon.CrossBackIcon
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.android.pass.featurecreateitem.impl.R
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemedBooleanPreviewProvider
import me.proton.pass.domain.ShareId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteTopBar(
    modifier: Modifier = Modifier,
    shareId: ShareId?,
    topBarTitle: String,
    topBarActionName: String,
    isLoadingState: IsLoadingState,
    onUpClick: () -> Unit,
    onEmitSnackbarMessage: (NoteSnackbarMessage) -> Unit,
    onSubmit: (ShareId) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    ProtonTopAppBar(
        modifier = modifier,
        title = { TopBarTitleView(title = topBarTitle) },
        navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
        actions = {
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    if (shareId == null) {
                        onEmitSnackbarMessage(NoteSnackbarMessage.EmptyShareIdError)
                    } else {
                        onSubmit(shareId)
                    }
                },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                when (isLoadingState) {
                    IsLoadingState.Loading -> { TopBarLoading() }
                    IsLoadingState.NotLoading -> {
                        Text(
                            text = topBarActionName,
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
fun NoteTopBarPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            NoteTopBar(
                isLoadingState = IsLoadingState.from(input.second),
                shareId = null,
                topBarTitle = stringResource(R.string.title_create_note),
                topBarActionName = stringResource(R.string.action_save),
                onUpClick = {},
                onSubmit = {},
                onEmitSnackbarMessage = {}
            )
        }
    }
}
