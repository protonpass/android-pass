package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Box
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun TotpProgress(
    modifier: Modifier = Modifier,
    remainingSeconds: Int,
    totalSeconds: Int
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = remainingSeconds.toFloat() / totalSeconds.toFloat(),
            color = ProtonTheme.colors.notificationSuccess,
            strokeWidth = 3.dp
        )
        Text(
            text = remainingSeconds.toInt().toString(),
            style = ProtonTheme.typography.defaultSmallWeak
        )
    }
}

@Preview
@Composable
fun TotpTimePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            TotpProgress(remainingSeconds = 2, totalSeconds = 4)
        }
    }
}
