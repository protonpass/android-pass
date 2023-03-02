package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer

@Composable
fun TitleVaultSelectionSection(
    modifier: Modifier = Modifier,
    showVaultSelector: Boolean,
    vaultName: String?,
    titleValue: String,
    onTitleRequiredError: Boolean,
    enabled: Boolean = true,
    onTitleChanged: (String) -> Unit,
    onVaultClicked: () -> Unit
) {
    if (showVaultSelector) {
        Column(
            modifier = modifier.roundedContainer(ProtonTheme.colors.separatorNorm)
        ) {
            VaultSelector(
                vaultName = vaultName ?: "",
                onVaultClicked = onVaultClicked
            )
            Divider()
            TitleSection(
                modifier = Modifier.padding(12.dp),
                value = titleValue,
                onTitleRequiredError = onTitleRequiredError,
                enabled = enabled,
                isRounded = true,
                onChange = onTitleChanged
            )
        }
    } else {
        TitleSection(
            modifier = modifier,
            value = titleValue,
            onTitleRequiredError = onTitleRequiredError,
            enabled = enabled,
            isRounded = false,
            onChange = onTitleChanged
        )
    }
}

@Preview
@Composable
fun TitleVaultSelectionSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            TitleVaultSelectionSection(
                showVaultSelector = input.second,
                vaultName = "Test vault",
                titleValue = "Some title",
                enabled = true,
                onTitleChanged = {},
                onTitleRequiredError = false,
                onVaultClicked = {}
            )
        }
    }
}
