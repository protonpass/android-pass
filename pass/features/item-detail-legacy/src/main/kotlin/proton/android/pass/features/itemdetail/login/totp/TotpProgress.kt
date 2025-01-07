/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.itemdetail.login.totp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

private const val PROGRESS_ANIMATION_LABEL = "TOTP Progress Animation"
private const val PROGRESS_ANIMATION_DURATION_MILLIS = 1_000

private const val PROGRESS_COLOR_ANIMATION_LABEL = "TOTP Progress Color Animation"
private const val PROGRESS_COLOR_ANIMATION_DURATION_MILLIS = 500

private const val PROGRESS_LIMIT_1 = 0f
private const val PROGRESS_LIMIT_2 = 0.2f
private const val PROGRESS_LIMIT_3 = 0.4f

@Composable
fun TotpProgress(
    modifier: Modifier = Modifier,
    remainingSeconds: Int,
    totalSeconds: Int
) {
    val currentProgress = remainingSeconds.toFloat() / totalSeconds.toFloat()

    val animatedProgress by animateFloatAsState(
        label = PROGRESS_ANIMATION_LABEL,
        targetValue = currentProgress,
        animationSpec = tween(
            durationMillis = PROGRESS_ANIMATION_DURATION_MILLIS,
            easing = LinearEasing
        )
    )

    val animatedProgressColor by animateColorAsState(
        label = PROGRESS_COLOR_ANIMATION_LABEL,
        targetValue = when (currentProgress) {
            in PROGRESS_LIMIT_2..PROGRESS_LIMIT_3 -> ProtonTheme.colors.notificationWarning
            in PROGRESS_LIMIT_1..PROGRESS_LIMIT_2 -> ProtonTheme.colors.notificationError
            else -> ProtonTheme.colors.notificationSuccess
        },
        animationSpec = tween(
            durationMillis = PROGRESS_COLOR_ANIMATION_DURATION_MILLIS,
            easing = LinearEasing
        )
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = animatedProgress,
            color = animatedProgressColor,
            strokeWidth = 3.dp
        )

        Text(
            text = remainingSeconds.toString(),
            style = ProtonTheme.typography.defaultSmallNorm
        )
    }
}

@Preview
@Composable
fun TotpTimePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            TotpProgress(remainingSeconds = 2, totalSeconds = 4)
        }
    }
}
