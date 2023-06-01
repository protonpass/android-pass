package proton.android.pass.featureitemdetail.impl.login.totp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.SectionSubtitle
import proton.android.pass.featureitemdetail.impl.login.TotpUiState
import me.proton.core.presentation.R as CoreR

@Composable
fun TotpRowContent(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.totp_section_title),
    state: TotpUiState.Visible,
    onCopyTotpClick: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCopyTotpClick(state.code) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_lock),
            contentDescription = stringResource(R.string.totp_icon_content_description),
            tint = PassTheme.colors.loginInteractionNormMajor1
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(text = label)
            val half = state.code.length / 2
            SectionSubtitle(
                text = (state.code.take(half) + "•" + state.code.takeLast(half)).asAnnotatedString()
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TotpProgress(
            remainingSeconds = state.remainingSeconds,
            totalSeconds = state.totalSeconds
        )
    }
}

@Preview
@Composable
fun TotpRowContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TotpRowContent(
                state = TotpUiState.Visible(
                    code = "123456",
                    remainingSeconds = 10,
                    totalSeconds = 30
                ),
                onCopyTotpClick = {}
            )
        }
    }
}
