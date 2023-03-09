package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.applyIf

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingCircleButton(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
    color: Color,
    isLoading: Boolean,
    buttonEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(CircleShape)
            .applyIf(condition = !isLoading && buttonEnabled, ifTrue = { clickable { onClick() } })
            .background(color)
    ) {
        AnimatedContent(modifier = Modifier.padding(16.dp, 10.dp), targetState = isLoading) {
            if (it) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = ProtonTheme.colors.iconInverted
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leadingIcon?.invoke()
                    text()
                }
            }
        }
    }
}
