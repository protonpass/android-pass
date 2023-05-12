package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography

@Composable
fun GeneratePasswordSelectorRow(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    iconContentDescription: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
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
                text = value,
                color = PassTheme.colors.textNorm,
                fontSize = 16.sp
            )

            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_chevron_down_filled),
                contentDescription = iconContentDescription,
                tint = PassTheme.colors.textHint
            )
        }
    }
}
