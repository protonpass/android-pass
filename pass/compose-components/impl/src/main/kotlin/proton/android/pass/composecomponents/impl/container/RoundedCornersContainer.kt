package proton.android.pass.composecomponents.impl.container

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.applyIf

@Composable
fun RoundedCornersColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cornerShape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(cornerShape)
            .applyIf(
                condition = onClick != null,
                ifTrue = { clickable { onClick?.invoke() } }
            )
            .border(
                width = 1.dp,
                color = ProtonTheme.colors.separatorNorm,
                shape = cornerShape
            ),
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
