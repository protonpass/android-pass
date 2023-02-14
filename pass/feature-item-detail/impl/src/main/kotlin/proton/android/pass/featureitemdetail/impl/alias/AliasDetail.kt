package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.featureitemdetail.impl.ItemDetailTopBar
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AliasDetail(
    modifier: Modifier = Modifier,
    item: Item,
    viewModel: AliasDetailViewModel = hiltViewModel(),
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit
) {
    LaunchedEffect(item) {
        viewModel.setItem(item)
    }

    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        topBar = {
            ItemDetailTopBar(
                color = PassColors.GreenAccent,
                onUpClick = onUpClick,
                onEditClick = { onEditClick(item.shareId, item.id, item.itemType) },
                onOptionsClick = {}
            )
        }
    ) { padding ->
        AliasDetailContent(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            state = viewState,
            onCopyAlias = { viewModel.onCopyAlias(it) }
        )
    }
}
