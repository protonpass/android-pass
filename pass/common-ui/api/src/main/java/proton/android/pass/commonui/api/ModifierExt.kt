package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip

fun Modifier.applyIf(
    condition: Boolean,
    ifTrue: @Composable Modifier.() -> Modifier,
    ifFalse: @Composable (Modifier.() -> Modifier)? = null
): Modifier =
    composed {
        when {
            condition -> then(ifTrue(Modifier))
            ifFalse != null -> then(ifFalse(Modifier))
            else -> this
        }
    }

fun Modifier.squircle(): Modifier = composed { clip(PassTheme.shapes.squircleShape) }
