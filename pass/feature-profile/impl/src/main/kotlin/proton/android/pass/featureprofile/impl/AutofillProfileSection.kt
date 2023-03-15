package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer

@Composable
fun AutofillProfileSection(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    onClick: (Boolean) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.roundedContainer(ProtonTheme.colors.separatorNorm)
        ) {
            ProfileToggle(
                text = stringResource(R.string.profile_option_autofill),
                isChecked = isChecked,
                onClick = { onClick(isChecked) }
            )
        }
        Column {
            Text(
                text = stringResource(R.string.profile_option_autofill_subtitle),
                style = ProtonTheme.typography.caption.copy(PassTheme.colors.textWeak)
            )
            Text(
                text = stringResource(R.string.profile_option_autofill_link),
                style = ProtonTheme.typography.caption.copy(PassTheme.colors.accentBrandOpaque)
            )
        }
    }
}

@Preview
@Composable
fun AutofillProfileSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AutofillProfileSection(isChecked = true) {}
        }
    }
}
