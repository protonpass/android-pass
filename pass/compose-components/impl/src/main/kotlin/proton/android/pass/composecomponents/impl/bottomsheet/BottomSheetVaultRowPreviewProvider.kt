package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

class BottomSheetVaultRowPreviewProvider : PreviewParameterProvider<VaultRowInput> {
    override val values: Sequence<VaultRowInput>
        get() = sequence {
            for (isSelected in listOf(true, false)) {
                for (enabled in listOf(true, false)) {
                    for (isLoading in listOf(true, false)) {
                        yield(
                            VaultRowInput(
                                vault = VaultWithItemCount(
                                    vault = Vault(
                                        shareId = ShareId("123"),
                                        name = "some vault",
                                        color = ShareColor.Color2,
                                        icon = ShareIcon.Icon10,
                                        isPrimary = false
                                    ),
                                    activeItemCount = 2,
                                    trashedItemCount = 0
                                ),
                                isSelected = isSelected,
                                enabled = enabled,
                                isLoading = isLoading
                            )
                        )
                    }
                }
            }
        }
}

data class VaultRowInput(
    val vault: VaultWithItemCount,
    val isSelected: Boolean,
    val enabled: Boolean,
    val isLoading: Boolean
)
