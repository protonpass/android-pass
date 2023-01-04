package me.proton.android.pass.composecomponents.impl.item

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun DropDownAction(
    title: String,
    textColor: Color = ProtonTheme.colors.textNorm,
    @DrawableRes icon: Int,
    onClick: () -> Unit
) {
    DropdownMenuItem(onClick = onClick) {
        Row {
            Text(
                text = title,
                color = textColor,
                fontWeight = FontWeight.W400
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painterResource(icon),
                contentDescription = null,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
