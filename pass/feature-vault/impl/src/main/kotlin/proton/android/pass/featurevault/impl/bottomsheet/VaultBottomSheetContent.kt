package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.uievents.value
import proton.android.pass.feature.vault.impl.R
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

@Composable
fun VaultBottomSheetContent(
    modifier: Modifier = Modifier,
    state: BaseVaultUiState,
    showUpgradeUi: Boolean,
    buttonText: String,
    onNameChange: (String) -> Unit,
    onColorChange: (ShareColor) -> Unit,
    onIconChange: (ShareIcon) -> Unit,
    onClose: () -> Unit,
    onButtonClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        CreateVaultBottomSheetTopBar(
            showUpgradeButton = showUpgradeUi,
            buttonText = buttonText,
            isLoading = state.isLoading.value(),
            isButtonEnabled = showUpgradeUi || state.isCreateButtonEnabled.value(),
            onCloseClick = onClose,
            onCreateClick = onButtonClick,
            onUpgradeClick = onUpgradeClick
        )

        AnimatedVisibility(visible = showUpgradeUi) {
            Column {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                        .background(PassTheme.colors.interactionNormMinor1)
                        .padding(16.dp),
                    text = stringResource(R.string.bottomsheet_cannot_create_more_vaults),
                    color = PassTheme.colors.textNorm,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        VaultPreviewSection(
            state = state,
            onNameChange = onNameChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        ColorSelectionSection(
            selected = state.color,
            onColorSelected = onColorChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        IconSelectionSection(
            selected = state.icon,
            onIconSelected = onIconChange
        )
    }
}
