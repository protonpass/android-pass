package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

const val TOTP_NAV_PARAMETER_KEY = "totp"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreateManualTotp(
    modifier: Modifier = Modifier,
    onAddManualTotp: (String) -> Unit,
    onCloseManualTotp: () -> Unit,
    viewModel: CreateManualTotpViewModel = hiltViewModel()
) {
    val uiState: CreateManualTotpUiState by viewModel.state.collectAsStateWithLifecycle()

    when (val state = uiState.isTotpUriCreatedState) {
        is IsTotpUriCreatedState.Success -> LaunchedEffect(state) { onAddManualTotp(state.totpUri) }
        else -> {}
    }

    CreateManualTotpContent(
        modifier = modifier,
        totpSpec = uiState.totpSpec,
        validationErrors = uiState.validationErrors,
        isLoadingState = uiState.isLoadingState,
        onAddManualTotp = { viewModel.onAddManualTotp(it) },
        onCloseManualTotp = onCloseManualTotp,
        onSecretChange = { viewModel.onSecretChange(it) },
        onLabelChange = { viewModel.onLabelChange(it) },
        onIssuerChange = { viewModel.onIssuerChange(it) },
        onAlgorithmChange = { viewModel.onAlgorithmChange(it) },
        onDigitsChange = { viewModel.onDigitsChange(it) },
        onValidPeriodChange = { viewModel.onValidPeriodChange(it) }
    )
}

