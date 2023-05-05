package proton.android.pass.featureitemdetail.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.featureitemdetail.impl.alias.AliasDetail
import proton.android.pass.featureitemdetail.impl.login.LoginDetail
import proton.android.pass.featureitemdetail.impl.note.NoteDetail

@Composable
fun ItemDetailContent(
    modifier: Modifier = Modifier,
    uiState: ItemDetailScreenUiState,
    onNavigate: (ItemDetailNavigation) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PassTheme.colors.backgroundStrong)
    ) {
        when (uiState.itemTypeUiState) {
            ItemTypeUiState.Login -> LoginDetail(
                moreInfoUiState = uiState.moreInfoUiState,
                onNavigate = onNavigate
            )
            ItemTypeUiState.Note -> NoteDetail(
                moreInfoUiState = uiState.moreInfoUiState,
                onNavigate = onNavigate
            )
            ItemTypeUiState.Alias -> AliasDetail(
                moreInfoUiState = uiState.moreInfoUiState,
                onNavigate = onNavigate
            )
            ItemTypeUiState.Password -> {}
            else -> {}
        }
    }
}
