package proton.android.pass.featurevault.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.defaultStrong
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.TopBarLoading
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.android.pass.composecomponents.impl.topbar.icon.CrossBackIcon
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.feature.vault.impl.R
import proton.android.pass.featurevault.impl.DraftVaultValidationErrors.BlankTitle

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateVaultContent(
    modifier: Modifier = Modifier,
    uiState: CreateVaultUIState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCreate: (DraftVaultUiState) -> Unit,
    onUpClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                modifier = modifier,
                title = { TopBarTitleView(title = stringResource(id = R.string.vault_create_top_bar_title)) },
                navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
                actions = {
                    IconButton(
                        enabled = uiState.isLoadingState == IsLoadingState.NotLoading,
                        onClick = { onCreate(uiState.draftVault) },
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        when (uiState.isLoadingState) {
                            IsLoadingState.Loading -> TopBarLoading()
                            IsLoadingState.NotLoading -> Text(
                                text = stringResource(id = R.string.vault_create_action_create),
                                style = ProtonTheme.typography.defaultStrong
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ProtonTextField(
                modifier = Modifier.padding(top = 8.dp),
                textStyle = ProtonTheme.typography.default,
                label = { ProtonTextFieldLabel(text = stringResource(id = R.string.vault_create_form_title_label)) },
                placeholder = {
                    ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.vault_create_form_title_placeholder))
                },
                value = uiState.draftVault.title,
                onChange = onTitleChange,
                isError = uiState.validationErrors.contains(BlankTitle)
            )
            ProtonTextField(
                modifier = Modifier.padding(top = 8.dp),
                textStyle = ProtonTheme.typography.default,
                label = {
                    ProtonTextFieldLabel(text = stringResource(id = R.string.vault_create_form_description_label))
                },
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(id = R.string.vault_create_form_description_placeholder)
                    )
                },
                value = uiState.draftVault.description,
                onChange = onDescriptionChange
            )
        }
    }
}
