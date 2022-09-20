package me.proton.android.pass.ui.create.alias

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
internal fun UpdateAlias(
    shareId: ShareId,
    itemId: ItemId,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onStart(shareId, itemId)
    }

    val viewState by rememberFlowWithLifecycle(viewModel.viewState)
        .collectAsState(viewModel.initialViewState)

    AliasContent(
        viewState = viewState,
        topBarTitle = R.string.title_edit_alias,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.updateAlias() },
        viewModel = viewModel,
        canEdit = false
    )
}
