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
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersContainer
import proton.android.pass.featureitemdetail.impl.DetailSectionSubtitle
import proton.android.pass.featureitemdetail.impl.DetailSectionTitle
import proton.android.pass.featureitemdetail.impl.R

@Composable
fun LoginUsernameRow(
    modifier: Modifier = Modifier,
    username: String,
    onUsernameClick: () -> Unit
) {
    RoundedCornersContainer(
        modifier = modifier.fillMaxWidth(),
        onClick = onUsernameClick
    ) {
        Column {
            DetailSectionTitle(text = stringResource(R.string.field_username))
            Spacer(modifier = Modifier.height(8.dp))
            DetailSectionSubtitle(text = username)
        }
    }
}

@Preview
@Composable
fun LoginUsernameRowPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            LoginUsernameRow(
                username = "some.username",
                onUsernameClick = {}
            )
        }
    }
}
