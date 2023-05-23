package proton.android.pass.featurevault.impl.bottomsheet.options

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.pass.domain.ShareId

class VaultOptionsBottomSheetContentsPreviewProvider :
    PreviewParameterProvider<VaultOptionsUiState.Success> {
    override val values: Sequence<VaultOptionsUiState.Success>
        get() = sequenceOf(
            VaultOptionsUiState.Success(
                shareId = ShareId(""),
                showEdit = true,
                showMigrate = true,
                showDelete = true
            )
        )
}
