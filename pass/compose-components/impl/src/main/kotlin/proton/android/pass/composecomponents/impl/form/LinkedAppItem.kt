package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.presentation.R
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.AndroidUtils.getApplicationIcon
import proton.android.pass.commonui.api.AndroidUtils.getApplicationName

@Composable
fun LinkedAppItem(
    modifier: Modifier = Modifier,
    packageName: String,
    isEditable: Boolean,
    onLinkedAppDelete: (String) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val context = LocalContext.current
        val iconDrawable = remember(packageName) { getApplicationIcon(context, packageName) }
        val appName = remember(packageName) { getApplicationName(context, packageName) }
        when (iconDrawable) {
            None -> Spacer(modifier = Modifier.width(24.dp))
            is Some -> Image(
                modifier = Modifier.width(24.dp),
                painter = rememberDrawablePainter(drawable = iconDrawable.value),
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = appName.value() ?: packageName,
            style = ProtonTheme.typography.default
        )
        if (isEditable) {
            IconButton(onClick = { onLinkedAppDelete(packageName) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_proton_minus_circle),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    }
}
