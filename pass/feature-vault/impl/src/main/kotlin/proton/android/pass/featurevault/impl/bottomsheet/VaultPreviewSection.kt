package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Composable
fun VaultPreviewSection(
    modifier: Modifier = Modifier,
    state: CreateVaultUiState,
    onNameChange: (String) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultIcon(
            backgroundColor = state.color.toColor(true),
            iconColor = state.color.toColor(),
            icon = state.icon.toResource()
        )

        TitleSection(
            value = state.name,
            onTitleRequiredError = state.isTitleRequiredError,
            enabled = state.isLoading == IsLoadingState.NotLoading,
            onChange = onNameChange
        )
    }
}

class ThemeVaultPreviewProvider : ThemePairPreviewProvider<CreateVaultUiState>(CreateVaultProvider())

@Preview
@Composable
fun VaultPreviewSectionPreview(
    @PreviewParameter(ThemeVaultPreviewProvider::class) input: Pair<Boolean, CreateVaultUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            VaultPreviewSection(
                state = input.second,
                onNameChange = {}
            )
        }
    }
}
