package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R

@Composable
internal fun Selector(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextField(
        readOnly = true,
        enabled = false,
        value = text,
        onValueChange = {},
        trailingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_chevron_right),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm
            )
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = TextFieldDefaults.textFieldColors(
            textColor = ProtonTheme.colors.textNorm,
            disabledTextColor = ProtonTheme.colors.textNorm,
            backgroundColor = ProtonTheme.colors.backgroundSecondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
