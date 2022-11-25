package me.proton.pass.presentation.detail.login

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme

@Composable
internal fun Section(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @DrawableRes icon: Int? = null,
    content: String,
    contentTextColor: Color = ProtonTheme.colors.textWeak,
    onIconClick: (() -> Unit)? = null,
    viewBelow: @Composable (() -> Unit)? = null
) {
    Row(modifier = modifier.padding(vertical = 12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            SectionTitle(title)
            Text(
                text = content,
                color = contentTextColor,
                fontSize = 14.sp
            )

            if (viewBelow != null) {
                viewBelow()
            }
        }
        if (icon != null) {
            IconButton(
                onClick = { onIconClick?.invoke() },
                modifier = Modifier.then(
                    Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            ) {
                Icon(painter = painterResource(icon), contentDescription = null)
            }
        }
    }
}

