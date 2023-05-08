package proton.android.pass.featurevault.impl.bottomsheet

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.feature.vault.impl.R
import proton.android.pass.featurevault.impl.VaultNavigation

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreateVaultBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (VaultNavigation) -> Unit,
    viewModel: CreateVaultViewModel = hiltViewModel()
) {
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val state = createState.base

    BackHandler {
        onNavigate(VaultNavigation.Close)
    }

    LaunchedEffect(state.isVaultCreatedEvent) {
        if (state.isVaultCreatedEvent == IsVaultCreatedEvent.Created) {
            onNavigate(VaultNavigation.Close)
        }
    }

    VaultBottomSheetContent(
        modifier = modifier
            .bottomSheet(horizontalPadding = PassTheme.dimens.bottomsheetHorizontalPadding),
        state = state,
        showUpgradeUi = createState.displayNeedUpgrade,
        buttonText = stringResource(R.string.bottomsheet_create_vault_button),
        onNameChange = { viewModel.onNameChange(it) },
        onIconChange = { viewModel.onIconChange(it) },
        onColorChange = { viewModel.onColorChange(it) },
        onClose = { onNavigate(VaultNavigation.Close) },
        onButtonClick = { viewModel.onCreateClick() },
        onUpgradeClick = { onNavigate(VaultNavigation.Upgrade) }
    )
}
