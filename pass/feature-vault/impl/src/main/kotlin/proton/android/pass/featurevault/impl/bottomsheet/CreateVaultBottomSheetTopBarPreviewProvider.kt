package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class CreateVaultBottomSheetTopBarPreviewProvider : PreviewParameterProvider<TopBarInput> {
    override val values: Sequence<TopBarInput>
        get() = sequenceOf(
            TopBarInput(isLoading = true, showUpgrade = false),
            TopBarInput(isLoading = false, showUpgrade = false),
            TopBarInput(isLoading = false, showUpgrade = true),
        )
}

data class TopBarInput(
    val isLoading: Boolean,
    val showUpgrade: Boolean
)
