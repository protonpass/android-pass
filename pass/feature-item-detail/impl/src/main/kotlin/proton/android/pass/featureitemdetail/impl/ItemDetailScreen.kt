package proton.android.pass.featureitemdetail.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ItemDetailScreen(
    modifier: Modifier = Modifier,
    onNavigate: (ItemDetailNavigation) -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.itemTypeUiState) {
        viewModel.sendItemReadEvent(uiState.itemTypeUiState)
    }

    ItemDetailContent(
        modifier = modifier,
        uiState = uiState,
        onNavigate = onNavigate
    )
}

