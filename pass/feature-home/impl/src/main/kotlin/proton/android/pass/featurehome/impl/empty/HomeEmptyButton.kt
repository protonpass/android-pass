package proton.android.pass.featurehome.impl.empty

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTypography

@Composable
fun HomeEmptyButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Icon(
            modifier = Modifier.align(Alignment.CenterStart),
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = textColor
        )
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = text,
            color = textColor,
            textAlign = TextAlign.Center,
            style = PassTypography.body3Regular
        )
    }
}
