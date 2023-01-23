package proton.android.pass.featurecreateitem.impl.note

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
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.topbar.TopBarLoading
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.android.pass.composecomponents.impl.topbar.icon.CrossBackIcon
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.R
import proton.pass.domain.ShareId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteTopBar(
    modifier: Modifier = Modifier,
    shareUiModel: ShareUiModel?,
    topBarTitle: String,
    topBarActionName: String,
    isLoadingState: IsLoadingState,
    onUpClick: () -> Unit,
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
                enabled = isLoadingState == IsLoadingState.NotLoading && shareUiModel != null,
                onClick = {
                    shareUiModel?.id?.let {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onSubmit(it)
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
                shareUiModel = ShareUiModel(ShareId(""), ""),
                topBarTitle = stringResource(R.string.title_create_note),
                topBarActionName = stringResource(R.string.action_save),
                onUpClick = {},
                onSubmit = {}
            )
        }
    }
}
