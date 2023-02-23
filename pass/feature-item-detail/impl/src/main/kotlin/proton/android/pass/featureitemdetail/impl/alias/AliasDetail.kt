package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.TopBarOptionsBottomSheetContents
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@OptIn(
    ExperimentalLifecycleComposeApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun AliasDetail(
    modifier: Modifier = Modifier,
    item: Item,
    moreInfoUiState: MoreInfoUiState,
    viewModel: AliasDetailViewModel = hiltViewModel(),
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit
) {
    LaunchedEffect(item) {
        viewModel.setItem(item)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isItemSentToTrash) {
        LaunchedEffect(Unit) { onUpClick() }
    }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            TopBarOptionsBottomSheetContents(
                onMoveToTrash = {
                    viewModel.onDelete(item.shareId, item.id)
                    scope.launch { bottomSheetState.hide() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                ItemDetailTopBar(
                    isLoading = uiState.isLoadingState,
                    color = PassTheme.colors.accentGreenOpaque,
                    onUpClick = onUpClick,
                    onEditClick = { onEditClick(item.shareId, item.id, item.itemType) },
                    onOptionsClick = {
                        scope.launch { bottomSheetState.show() }
                    }
                )
            }
        ) { padding ->
            AliasDetailContent(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                model = uiState.model,
                isLoading = uiState.isLoadingState,
                onCopyAlias = { viewModel.onCopyAlias(it) },
                moreInfoUiState = moreInfoUiState
            )
        }
    }
}
