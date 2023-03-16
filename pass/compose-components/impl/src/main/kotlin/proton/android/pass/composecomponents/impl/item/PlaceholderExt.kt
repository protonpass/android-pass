package proton.android.pass.composecomponents.impl.item

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import proton.android.pass.commonui.api.PassTheme

fun Modifier.placeholder(): Modifier =
    composed {
        placeholder(
            visible = true,
            color = PassTheme.colors.backgroundWeak,
            shape = PassTheme.shapes.squircleShape,
            highlight = PlaceholderHighlight.shimmer(
                highlightColor = PassTheme.colors.backgroundNorm
            )
        )
    }
