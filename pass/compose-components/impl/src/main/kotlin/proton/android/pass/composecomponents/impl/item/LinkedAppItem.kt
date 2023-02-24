package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.AndroidUtils.getApplicationIcon
import proton.android.pass.commonui.api.AndroidUtils.getApplicationName
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton

@Composable
fun LinkedAppItem(
    modifier: Modifier = Modifier,
    packageInfoUi: PackageInfoUi,
    isEditable: Boolean,
    onLinkedAppDelete: (PackageInfoUi) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val context = LocalContext.current
        val iconDrawable = remember(packageInfoUi.packageName) {
            getApplicationIcon(context, packageInfoUi.packageName)
        }
        val appName = remember(packageInfoUi.packageName) {
            packageInfoUi.appName.ifBlank {
                getApplicationName(context, packageInfoUi.packageName).value()
                    ?: packageInfoUi.packageName
            }
        }
        when (iconDrawable) {
            None -> Circle(backgroundColor = PassTheme.colors.accentPurpleOpaque) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_grid_3),
                    contentDescription = stringResource(R.string.missing_app_icon_content_description),
                    tint = PassTheme.colors.accentPurpleOpaque
                )
            }
            is Some -> Image(
                modifier = Modifier.width(40.dp),
                painter = rememberDrawablePainter(drawable = iconDrawable.value),
                contentDescription = stringResource(R.string.linked_app_icon_content_description)
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = appName,
            style = ProtonTheme.typography.default
        )
        if (isEditable) {
            SmallCrossIconButton { onLinkedAppDelete(packageInfoUi) }
        }
    }
}
