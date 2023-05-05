package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class VaultOptionsBottomSheetContentsPreviewProvider : PreviewParameterProvider<VaultOptionsInput> {
    override val values: Sequence<VaultOptionsInput>
        get() = sequence {
            for (migrate in listOf(true, false)) {
                for (delete in listOf(true, false)) {
                    yield(VaultOptionsInput(migrate, delete))
                }
            }
        }
}

data class VaultOptionsInput(
    val showMigrate: Boolean,
    val showDelete: Boolean
)
