/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.password.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider

private const val SLIDER_NORMALIZATION_FACTOR = 100

private const val SLIDER_WEIGHT_CONTENT = 0.65f
private const val SLIDER_WEIGHT_TEXT = 0.35f

@Composable
internal fun GeneratePasswordSliderRow(
    modifier: Modifier = Modifier,
    text: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    normalizationFactor: Int = SLIDER_NORMALIZATION_FACTOR
) {
    val normalizationCoefficient = remember(maxValue, minValue) {
        normalizationFactor / (maxValue - minValue + 1).coerceAtLeast(minimumValue = 1)
    }

    val sliderValueRange = remember(normalizationCoefficient) {
        val min = minValue.toFloat().times(normalizationCoefficient)
        val max = maxValue.toFloat().times(normalizationCoefficient)
        min..max
    }

    var sliderValue by remember {
        mutableFloatStateOf(value.toFloat().times(normalizationCoefficient))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(weight = SLIDER_WEIGHT_TEXT),
            text = text,
            color = PassTheme.colors.textNorm,
            style = ProtonTheme.typography.defaultSmallNorm
        )

        Slider(
            modifier = Modifier.weight(weight = SLIDER_WEIGHT_CONTENT),
            value = sliderValue,
            valueRange = sliderValueRange,
            colors = SliderDefaults.colors(
                thumbColor = PassTheme.colors.loginInteractionNormMajor1,
                activeTrackColor = PassTheme.colors.loginInteractionNormMajor1,
                inactiveTrackColor = PassTheme.colors.loginInteractionNormMinor1
            ),
            onValueChange = { newValue ->
                sliderValue = newValue
                onValueChange(newValue.div(normalizationCoefficient).toInt())
            }
        )
    }
}

@[Preview Composable]
internal fun GeneratePasswordSliderRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            GeneratePasswordSliderRow(
                text = "text",
                value = 4,
                minValue = 1,
                maxValue = 10,
                onValueChange = {}
            )
        }
    }
}
