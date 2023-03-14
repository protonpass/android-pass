package proton.android.pass.featureitemcreate.impl.totp.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun CameraPermissionContent(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(Unit) { onRequestPermission() }
    Box(modifier = modifier.fillMaxSize()) {
        SmallCrossIconButton(modifier = Modifier.align(Alignment.TopEnd)) { onDismiss() }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.width(250.dp),
                text = stringResource(R.string.camera_permission_explanation),
                style = ProtonTheme.typography.default,
                textAlign = TextAlign.Center
            )
            CircleButton(
                modifier = Modifier.width(250.dp),
                color = PassTheme.colors.accentPurpleOpaque,
                onClick = { onOpenAppSettings() }
            ) {
                Text(
                    text = stringResource(R.string.camera_permission_open_settings),
                    style = PassTypography.body3RegularInverted
                )
            }
        }
    }

}

@Preview
@Composable
fun CameraPermissionContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            CameraPermissionContent(
                onRequestPermission = {},
                onOpenAppSettings = {},
                onDismiss = {}
            )
        }
    }
}
