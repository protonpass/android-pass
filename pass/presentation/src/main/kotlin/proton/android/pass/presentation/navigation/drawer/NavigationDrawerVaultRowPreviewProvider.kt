package proton.android.pass.presentation.navigation.drawer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class NavigationDrawerVaultRowPreviewProvider : PreviewParameterProvider<VaultRowInput> {
    override val values: Sequence<VaultRowInput>
        get() = sequence {
            for (shared in listOf(true, false)) {
                for (selected in listOf(true, false)) {
                    yield(
                        VaultRowInput(
                            isShared = shared,
                            isSelected = selected,
                            showMenuIcon = true
                        )
                    )
                }
            }
            VaultRowInput(
                isShared = false,
                isSelected = false,
                showMenuIcon = false
            )
        }
}

data class VaultRowInput(
    val isShared: Boolean,
    val isSelected: Boolean,
    val showMenuIcon: Boolean
)
