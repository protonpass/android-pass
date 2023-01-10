package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import me.proton.core.compose.theme.ProtonTheme

fun Modifier.placeholder(): Modifier =
    composed {
        placeholder(
            visible = true,
            color = ProtonTheme.colors.interactionWeakNorm,
            shape = RoundedCornerShape(8.dp),
            highlight = PlaceholderHighlight.shimmer(
                highlightColor = ProtonTheme.colors.interactionWeakPressed
            )
        )
    }
