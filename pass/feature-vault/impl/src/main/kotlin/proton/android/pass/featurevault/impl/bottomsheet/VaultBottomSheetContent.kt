package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.uievents.value
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

@Composable
fun VaultBottomSheetContent(
    modifier: Modifier = Modifier,
    state: CreateVaultUiState,
    buttonText: String,
    onNameChange: (String) -> Unit,
    onColorChange: (ShareColor) -> Unit,
    onIconChange: (ShareIcon) -> Unit,
    onClose: () -> Unit,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        CreateVaultBottomSheetTopBar(
            buttonText = buttonText,
            isLoading = state.isLoading.value(),
            isButtonEnabled = state.isCreateButtonEnabled.value(),
            onCloseClick = onClose,
            onCreateClick = onButtonClick
        )

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
