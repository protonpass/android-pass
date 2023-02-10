package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.container.RoundedCornersContainer
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.featureitemdetail.impl.DetailSectionSubtitle
import proton.android.pass.featureitemdetail.impl.DetailSectionTitle
import proton.android.pass.featureitemdetail.impl.R

@Suppress("UnusedPrivateMember") // Unused until design comes up with a solution for reveal/conceal
@Composable
internal fun LoginPasswordRow(
    modifier: Modifier = Modifier,
    password: PasswordState,
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit
) {
    val actionContent = when (password) {
        is PasswordState.Concealed -> stringResource(R.string.action_reveal_password)
        is PasswordState.Revealed -> stringResource(R.string.action_conceal_password)
    }

    val sectionContent = when (password) {
        is PasswordState.Concealed -> "⬤".repeat(12)
        is PasswordState.Revealed -> password.clearText
    }

    RoundedCornersContainer(
        modifier = modifier.fillMaxWidth(),
        onClick = onCopyPasswordClick
    ) {
        Column {
            DetailSectionTitle(text = stringResource(R.string.field_password))
            Spacer(modifier = Modifier.height(8.dp))
            DetailSectionSubtitle(text = sectionContent)
        }
    }
}

class ThemedLoginPasswordRowPreviewProvider :
    ThemePairPreviewProvider<PasswordState>(PasswordStatePreviewProvider())

@Preview
@Composable
fun LoginPasswordRowPreview(
    @PreviewParameter(ThemedLoginPasswordRowPreviewProvider::class) input: Pair<Boolean, PasswordState>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            LoginPasswordRow(
                password = input.second,
                onCopyPasswordClick = {},
                onTogglePasswordClick = {}
            )
        }
    }
}
