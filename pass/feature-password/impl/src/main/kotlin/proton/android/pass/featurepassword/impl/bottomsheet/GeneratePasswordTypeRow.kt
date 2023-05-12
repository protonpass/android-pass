package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.featurepassword.R
import proton.android.pass.featurepassword.impl.extensions.toResourceString
import proton.android.pass.preferences.PasswordGenerationMode
import me.proton.core.presentation.R as CoreR

@Composable
fun GeneratePasswordTypeRow(
    modifier: Modifier = Modifier,
    current: PasswordGenerationMode,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.password_type),
            color = PassTheme.colors.textNorm,
            style = PassTypography.body3Regular,
            fontSize = 16.sp
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = current.toResourceString(),
                color = PassTheme.colors.textNorm,
                fontSize = 16.sp
            )

            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_chevron_down_filled),
                contentDescription = stringResource(R.string.password_mode_icon),
                tint = PassTheme.colors.textHint
            )
        }
    }
}

@Preview
@Composable
fun GeneratePasswordTypeRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val current = if (input.second) PasswordGenerationMode.Random else PasswordGenerationMode.Words
    PassTheme(isDark = input.first) {
        Surface {
            GeneratePasswordTypeRow(
                current = current,
                onClick = {}
            )
        }
    }
}
