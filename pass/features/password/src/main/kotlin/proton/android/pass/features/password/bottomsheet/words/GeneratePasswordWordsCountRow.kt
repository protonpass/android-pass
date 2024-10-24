/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.password.bottomsheet.words

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.password.R

@Composable
internal fun GeneratePasswordWordsCountRow(
    modifier: Modifier = Modifier,
    count: Int,
    minCount: Int,
    maxCount: Int,
    onCountChange: (Int) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(count.toFloat()) }
    val valueRange = remember { minCount.toFloat()..maxCount.toFloat() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(SLIDER_TEXT_WEIGHT),
            text = pluralStringResource(R.plurals.word_count, count, count),
            color = PassTheme.colors.textNorm,
            style = ProtonTheme.typography.defaultSmallNorm
        )

        Slider(
            modifier = Modifier.weight(SLIDER_CONTENT_WEIGHT),
            value = sliderPosition,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = PassTheme.colors.loginInteractionNormMajor1,
                activeTrackColor = PassTheme.colors.loginInteractionNormMajor1,
                inactiveTrackColor = PassTheme.colors.loginInteractionNormMinor1
            ),
            onValueChange = { newLength ->
                if (sliderPosition.toInt() != newLength.toInt()) {
                    sliderPosition = newLength
                    onCountChange(newLength.toInt())
                }
            }
        )
    }
}

@[Preview Composable]
internal fun GeneratePasswordWordsCountRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            GeneratePasswordWordsCountRow(
                count = 4,
                minCount = 1,
                maxCount = 10,
                onCountChange = {}
            )
        }
    }
}

private const val SLIDER_CONTENT_WEIGHT = 0.65f
private const val SLIDER_TEXT_WEIGHT = 0.35f
