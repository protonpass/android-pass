package proton.android.pass.featurecreateitem.impl.alias

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
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.topbar.TopBarLoading
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.android.pass.composecomponents.impl.topbar.icon.CrossBackIcon
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.R
import proton.pass.domain.ShareId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AliasTopBar(
    modifier: Modifier = Modifier,
    @StringRes topBarTitle: Int,
    shareUiModel: ShareUiModel?,
    isDraft: Boolean,
    isButtonEnabled: IsButtonEnabled,
    isLoadingState: IsLoadingState,
    onUpClick: () -> Unit,
    onSubmit: (ShareId) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val (buttonEnabled, buttonTextColor) = when (isButtonEnabled) {
        IsButtonEnabled.Enabled -> true to ProtonTheme.colors.brandNorm
        IsButtonEnabled.Disabled -> false to ProtonTheme.colors.interactionDisabled
    }

    ProtonTopAppBar(
        modifier = modifier,
        title = { TopBarTitleView(title = topBarTitle) },
        navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
        actions = {
            IconButton(
                enabled = buttonEnabled && isLoadingState == IsLoadingState.NotLoading && shareUiModel != null,
                onClick = {
                    shareUiModel?.id?.let {
                        keyboardController?.hide()
                        onSubmit(it)
                    }
                },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                when (isLoadingState) {
                    IsLoadingState.Loading -> TopBarLoading()
                    IsLoadingState.NotLoading -> {
                        val saveText = if (isDraft) {
                            stringResource(R.string.alias_action_save_fill)
                        } else {
                            stringResource(R.string.action_save)
                        }
                        Text(
                            text = saveText,
                            color = buttonTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            }
        }
    )
}

class ThemedAliasTopBarPreviewProvider :
    ThemePairPreviewProvider<AliasTopBarInput>(AliasTopBarPreviewProvider())

@Preview
@Composable
fun AliasTopBarPreview(
    @PreviewParameter(ThemedAliasTopBarPreviewProvider::class) input: Pair<Boolean, AliasTopBarInput>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            AliasTopBar(
                topBarTitle = R.string.title_create_alias,
                isDraft = input.second.isDraft,
                isButtonEnabled = input.second.buttonEnabled,
                isLoadingState = input.second.isLoadingState,
                onUpClick = {},
                onSubmit = {},
                shareUiModel = ShareUiModel(ShareId(""), "")
            )
        }
    }
}
