package me.proton.pass.presentation.components.common.item

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.fade
import com.google.accompanist.placeholder.placeholder
import me.proton.core.compose.theme.ProtonTheme

fun Modifier.placeholder(): Modifier =
    composed {
        placeholder(
            visible = true,
            color = ProtonTheme.colors.backgroundDeep,
            shape = RoundedCornerShape(8.dp),
            highlight = PlaceholderHighlight.fade(
                highlightColor = ProtonTheme.colors.backgroundSecondary
            )
        )
    }
