package proton.android.pass.featureitemcreate.impl.bottomsheets.createitem

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavGraphBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ShareId
import javax.inject.Inject

object CreateItemBottomsheet : NavItem(
    baseRoute = "item/create/bottomsheet",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId)
) {
    fun createNavRoute(
        shareId: Option<ShareId> = None,
    ) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

fun NavGraphBuilder.bottomsheetCreateItemGraph(
    onCreateLogin: (Option<ShareId>) -> Unit,
    onCreateAlias: (Option<ShareId>) -> Unit,
    onCreateNote: (Option<ShareId>) -> Unit,
    onCreatePassword: () -> Unit
) {
    bottomSheet(CreateItemBottomsheet) {
        CreateItemBottomSheet(
            onCreateLogin = onCreateLogin,
            onCreateAlias = onCreateAlias,
            onCreateNote = onCreateNote,
            onCreatePassword = onCreatePassword
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreateItemBottomSheet(
    modifier: Modifier = Modifier,
    onCreateLogin: (Option<ShareId>) -> Unit,
    onCreateAlias: (Option<ShareId>) -> Unit,
    onCreateNote: (Option<ShareId>) -> Unit,
    onCreatePassword: () -> Unit,
    viewModel: CreateItemBottomSheetViewModel = hiltViewModel()
) {
    CreateItemBottomSheetContents(
        modifier = modifier,
        shareId = viewModel.navShareId.value(),
        onCreateLogin = onCreateLogin,
        onCreateAlias = onCreateAlias,
        onCreateNote = onCreateNote,
        onCreatePassword = onCreatePassword
    )
}

@HiltViewModel
class CreateItemBottomSheetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val navShareId = savedStateHandle.get<String>(CommonOptionalNavArgId.ShareId.key)
        .toOption()
        .map { ShareId(it) }
}
