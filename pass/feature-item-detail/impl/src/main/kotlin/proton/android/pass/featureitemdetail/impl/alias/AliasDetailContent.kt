package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.NoteSection

@Composable
fun AliasDetailContent(
    modifier: Modifier = Modifier,
    model: AliasUiModel?,
    moreInfoUiState: MoreInfoUiState,
    isLoading: Boolean,
    onCopyAlias: (String) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AliasTitle(title = model?.title ?: "")
        AliasSection(Modifier, model, isLoading, onCopyAlias)
        NoteSection(text = model?.note ?: "", accentColor = PassTheme.colors.accentGreenOpaque)
        MoreInfo(moreInfoUiState = moreInfoUiState)
    }
}

