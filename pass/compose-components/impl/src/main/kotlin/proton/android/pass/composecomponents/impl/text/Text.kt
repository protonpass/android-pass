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

package proton.android.pass.composecomponents.impl.text

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.theme.headlineNorm
import me.proton.core.compose.theme.overlineNorm
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Medium
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.body3Weak

object Text {

    @Composable
    fun Hero(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.hero.copy(color = color),
            modifier = modifier
        )
    }

    @Composable
    fun Headline(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.headlineNorm.copy(color = color),
            modifier = modifier,
            textAlign = textAlign
        )
    }

    @Composable
    fun Subheadline(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.subheadlineNorm.copy(color = color),
            modifier = modifier
        )
    }

    @Composable
    fun Body1Regular(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.defaultNorm.copy(color = color),
            modifier = modifier,
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign
        )
    }

    @Composable
    fun Body1Medium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.body1Medium.copy(color = color),
            modifier = modifier
        )
    }

    @Composable
    fun Body1Bold(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.body1Bold.copy(color = color),
            modifier = modifier
        )
    }

    @Composable
    fun Body1Weak(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textWeak,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.defaultWeak.copy(color = color),
            modifier = modifier,
            textAlign = textAlign
        )
    }

    @Composable
    fun Body2Medium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.body2Medium.copy(color = color),
            modifier = modifier,
            textAlign = textAlign
        )
    }

    @Composable
    fun Body2Bold(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.body2Regular.copy(
                color = color,
                fontWeight = FontWeight.W700
            ),
            modifier = modifier,
            textAlign = textAlign
        )
    }

    @Composable
    fun Body2Regular(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm,
        textAlign: TextAlign = TextAlign.Start,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.defaultSmallNorm.copy(color = color),
            modifier = modifier,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow
        )
    }

    @Composable
    fun Body3Medium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = PassTheme.typography.body3Medium().copy(color = color),
            modifier = modifier,
            textAlign = textAlign
        )
    }

    @Composable
    fun Body3Regular(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = PassTheme.typography.body3Norm().copy(color = color),
            modifier = modifier,
            textAlign = textAlign
        )
    }

    @Composable
    fun Body3Weak(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textWeak,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = PassTheme.typography.body3Weak().copy(color = color),
            modifier = modifier,
            textAlign = textAlign
        )
    }

    @Composable
    fun CaptionRegular(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.captionNorm.copy(color = color),
            modifier = modifier
        )
    }

    @Composable
    fun CaptionWeak(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = PassTheme.colors.textWeak,
        textAlign: TextAlign? = null
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.captionWeak.copy(color = color),
            modifier = modifier,
            textAlign = textAlign
        )
    }

    @Composable
    fun CaptionMedium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = PassTheme.colors.textNorm
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.captionMedium.copy(color = color),
            modifier = modifier
        )
    }

    @Composable
    fun OverlineRegular(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ProtonTheme.colors.textNorm
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.overlineNorm.copy(color = color),
            modifier = modifier
        )
    }
}
