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

package proton.android.pass.features.security.center.shared.ui.rows

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.security.center.shared.ui.counters.SecurityCenterCounterIcon
import proton.android.pass.features.security.center.shared.ui.counters.SecurityCenterCounterText
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SecurityCenterCounterRow(
    modifier: Modifier = Modifier,
    model: SecurityCenterCounterRowModel,
    onClick: (() -> Unit)? = null
) = when (model) {
    is SecurityCenterCounterRowModel.Alert -> {
        SecurityCenterRow(
            modifier = modifier,
            title = model.title,
            subtitle = model.subtitle,
            isClickable = model.isClickable,
            onClick = onClick,
            titleColor = model.getCounterTextColor(),
            subtitleColor = model.getCounterTextColor(),
            accentBackgroundColor = model.getAccentBackgroundColor(),
            leadingContent = {
                SecurityCenterCounterIcon(
                    iconPainter = model.getCounterIconPainter(),
                    iconColor = model.getCounterIconColor(),
                    iconBackgroundColor = model.getCounterIconBackgroundColor(),
                    shape = model.counterIconShape
                )
            },
            trailingContent = {
                SecurityCenterCounterText(
                    counterText = model.counterText,
                    backgroundColor = model.getCounterTextBackgroundColor(),
                    textColor = model.getCounterTextColor()
                )
            },
            chevronTintColor = model.getCounterTextColor()
        )

    }

    is SecurityCenterCounterRowModel.Indicator -> {
        SecurityCenterRow(
            modifier = modifier,
            title = model.title,
            subtitle = model.subtitle,
            isClickable = model.isClickable,
            onClick = onClick,
            accentBackgroundColor = model.getAccentBackgroundColor(),
            leadingContent = {
                SecurityCenterCounterIcon(
                    iconPainter = model.getCounterIconPainter(),
                    iconColor = model.getCounterIconColor(),
                    iconBackgroundColor = model.getCounterIconBackgroundColor()
                )
            },
            trailingContent = {
                SecurityCenterCounterText(
                    counterText = model.counterText,
                    backgroundColor = model.getCounterTextBackgroundColor(),
                    textColor = model.getCounterTextColor()
                )
            },
            chevronTintColor = model.getCounterTextColor()
        )
    }

    is SecurityCenterCounterRowModel.Standard -> {
        SecurityCenterRow(
            modifier = modifier,
            title = model.title,
            subtitle = model.subtitle,
            isClickable = model.isClickable,
            onClick = onClick,
            trailingContent = {
                SecurityCenterCounterText(
                    counterText = model.counterText,
                    backgroundColor = model.getCounterTextBackgroundColor(),
                    textColor = model.getCounterTextColor()
                )
            }
        )
    }

    is SecurityCenterCounterRowModel.Success -> {
        SecurityCenterRow(
            modifier = modifier,
            title = model.title,
            subtitle = model.subtitle,
            isClickable = model.isClickable,
            subtitleColor = model.getSubtitleColor(),
            onClick = onClick,
            leadingContent = {
                SecurityCenterCounterIcon(
                    iconPainter = model.getCounterIconPainter(),
                    iconColor = model.getCounterIconColor(),
                    iconBackgroundColor = model.getCounterIconBackgroundColor()
                )
            }
        )
    }
}

@Stable
internal sealed interface SecurityCenterCounterRowModel {

    val counterText: String

    val isClickable: Boolean

    @Stable
    data class Alert(
        internal val title: String,
        internal val subtitle: String,
        private val count: Int
    ) : SecurityCenterCounterRowModel {

        override val counterText: String = count.toString()

        override val isClickable: Boolean = count > 0

        internal val counterIconShape: Shape = CircleShape

        @Composable
        internal fun getCounterTextBackgroundColor(): Color =
            PassTheme.colors.passwordInteractionNormMinor1

        @Composable
        internal fun getCounterTextColor(): Color = PassTheme.colors.passwordInteractionNormMajor2

        @Composable
        internal fun getAccentBackgroundColor(): Color =
            PassTheme.colors.passwordInteractionNormMinor1

        @Composable
        internal fun getCounterIconPainter(): Painter =
            painterResource(id = CompR.drawable.ic_exclamation_mark)

        @Composable
        internal fun getCounterIconColor(): Color = PassTheme.colors.interactionNormMinor2

        @Composable
        internal fun getCounterIconBackgroundColor(): Color =
            PassTheme.colors.passwordInteractionNormMajor2

    }

    @Stable
    data class Indicator(
        internal val title: String,
        internal val subtitle: String,
        private val count: Int?
    ) : SecurityCenterCounterRowModel {

        override val counterText: String = count?.toString() ?: "-"

        override val isClickable: Boolean = if (count == null) false else count > 0

        @Composable
        internal fun getCounterTextBackgroundColor(): Color = when (count) {
            null -> PassTheme.colors.loginInteractionNormMinor1
            0 -> PassTheme.colors.cardInteractionNormMinor1
            else -> PassTheme.colors.noteInteractionNormMinor1
        }

        @Composable
        internal fun getCounterTextColor(): Color = when (count) {
            null -> PassTheme.colors.loginInteractionNormMajor2
            0 -> PassTheme.colors.cardInteractionNormMajor2
            else -> PassTheme.colors.noteInteractionNormMajor2
        }

        @Composable
        internal fun getAccentBackgroundColor(): Color? = when (count) {
            null,
            0 -> null

            else -> PassTheme.colors.noteInteractionNormMinor1
        }

        @Composable
        internal fun getCounterIconPainter(): Painter = when (count) {
            null -> CompR.drawable.ic_checkmark
            0 -> CompR.drawable.ic_checkmark
            else -> CompR.drawable.ic_exclamation_mark
        }.let { iconResId -> painterResource(id = iconResId) }

        @Composable
        internal fun getCounterIconColor(): Color = PassTheme.colors.interactionNormMinor2

        @Composable
        internal fun getCounterIconBackgroundColor(): Color = when (count) {
            null -> PassTheme.colors.loginInteractionNormMajor2
            0 -> PassTheme.colors.cardInteractionNormMajor2
            else -> PassTheme.colors.noteInteractionNormMajor2
        }

    }

    @Stable
    data class Standard(
        internal val title: String,
        internal val subtitle: String,
        private val count: Int?
    ) : SecurityCenterCounterRowModel {

        override val counterText: String = count?.toString() ?: "-"

        override val isClickable: Boolean = if (count == null) false else count > 0

        @Composable
        internal fun getCounterTextBackgroundColor(): Color = PassTheme.colors.backgroundMedium

        @Composable
        internal fun getCounterTextColor(): Color = PassTheme.colors.textNorm

    }

    @Stable
    data class Success(
        internal val title: String,
        internal val subtitle: String
    ) : SecurityCenterCounterRowModel {

        override val counterText: String = ""

        override val isClickable: Boolean = true

        @Composable
        internal fun getSubtitleColor(): Color = PassTheme.colors.cardInteractionNormMajor2

        @Composable
        internal fun getCounterIconPainter(): Painter =
            painterResource(id = CompR.drawable.ic_checkmark)

        @Composable
        internal fun getCounterIconColor(): Color = PassTheme.colors.interactionNormMinor2

        @Composable
        internal fun getCounterIconBackgroundColor(): Color =
            PassTheme.colors.cardInteractionNormMajor2

    }

}

@[Preview Composable]
fun SecurityCenterCounterRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecurityCenterCounterRow(
                model = SecurityCenterCounterRowModel.Indicator(
                    title = "Security center row counter title",
                    subtitle = "Security center row counter subtitle",
                    count = 0
                )
            )
        }
    }
}
