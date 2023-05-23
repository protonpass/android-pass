package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
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
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.AndroidUtils.getApplicationName
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.item.icon.LinkedAppIcon

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
        val appName = remember(packageInfoUi.packageName) {
            packageInfoUi.appName.ifBlank {
                getApplicationName(context, packageInfoUi.packageName).value()
                    ?: packageInfoUi.packageName
            }
        }

        LinkedAppIcon(
            packageName = packageInfoUi.packageName,
            shape = CircleShape,
            emptyContent = {
                Circle(backgroundColor = PassTheme.colors.loginInteractionNormMinor2) {
                    Icon(
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_grid_3),
                        contentDescription = stringResource(R.string.missing_app_icon_content_description),
                        tint = PassTheme.colors.loginInteractionNorm
                    )
                }
            }
        )
        Text(
            modifier = Modifier.weight(1f),
            text = appName,
            style = ProtonTheme.typography.defaultNorm
        )
        if (isEditable) {
            SmallCrossIconButton { onLinkedAppDelete(packageInfoUi) }
        }
    }
}
