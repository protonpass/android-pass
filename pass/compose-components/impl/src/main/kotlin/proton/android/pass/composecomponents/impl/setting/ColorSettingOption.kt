package proton.android.pass.composecomponents.impl.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton

@Composable
fun ColorSettingOption(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    iconBgColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.defaultNorm,
            color = textColor
        )
        CircleIconButton(
            backgroundColor = iconBgColor,
            onClick = onClick
        ) {
            icon()
        }
    }
}
