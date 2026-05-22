/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.composecomponents.impl.loading

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.text.Text

@Composable
fun ProgressWithLabel(
    modifier: Modifier = Modifier,
    label: String,
    progress: Float? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        CircularProgressIndicator()

        Text.CaptionWeak(text = label)

        if (progress != null) {
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = PassTheme.colors.interactionNormMajor1,
                backgroundColor = PassTheme.colors.backgroundMedium
            )
        }
    }
}

internal class ProgressPreviewProvider : PreviewParameterProvider<Float?> {
    override val values: Sequence<Float?> = sequenceOf(null, 0f, 0.3f, 0.99f)
}

internal class ThemedProgressPreviewProvider :
    ThemePairPreviewProvider<Float?>(ProgressPreviewProvider())

@[Preview Composable]
fun ProgressWithLabelPreview(@PreviewParameter(ThemedProgressPreviewProvider::class) input: Pair<Boolean, Float?>) {
    PassTheme(isDark = input.first) {
        Surface {
            ProgressWithLabel(
                label = "Building search index… (4500/15000)",
                progress = input.second
            )
        }
    }
}
