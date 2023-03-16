package proton.android.pass.composecomponents.impl.container

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.squircle

@Composable
fun Squircle(
    modifier: Modifier = Modifier,
    size: Int = 40,
    backgroundColor: Color,
    backgroundAlpha: Float = 0.25f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .squircle()
            .applyIf(
                condition = onClick != null,
                ifTrue = { clickable { onClick?.invoke() } }
            )
            .background(backgroundColor.copy(backgroundAlpha)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
