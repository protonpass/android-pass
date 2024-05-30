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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.icon.PassPlusIcon
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
                    icon = model.getCounterIcon(),
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
                    icon = model.getCounterIcon(),
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
            accentBackgroundColor = PassTheme.colors.interactionNormMinor2.takeIf { model.showPassPlusIcon },
            onClick = onClick,
            trailingContent = {
                SecurityCenterCounterText(
                    counterText = model.counterText,
                    backgroundColor = model.getCounterTextBackgroundColor(),
                    textColor = model.getCounterTextColor()
                )

                if (model.showPassPlusIcon) {
                    PassPlusIcon()
                }
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
                    icon = model.getCounterIcon(),
                    iconColor = model.getCounterIconColor(),
                    iconBackgroundColor = model.getCounterIconBackgroundColor()
                )
            }
        )
    }

    is SecurityCenterCounterRowModel.Loading ->
        SecurityCenterRow(
            modifier = modifier,
            title = model.title,
            subtitle = "",
            isClickable = model.isClickable,
            isLoading = true,
            onClick = onClick,
            leadingContent = {}
        )
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
        internal fun getCounterTextBackgroundColor(): Color = PassTheme.colors.passwordInteractionNormMinor1

        @Composable
        internal fun getCounterTextColor(): Color = PassTheme.colors.passwordInteractionNormMajor2

        @Composable
        internal fun getAccentBackgroundColor(): Color = PassTheme.colors.passwordInteractionNormMinor2

        @Composable
        @DrawableRes
        internal fun getCounterIcon(): Int = CompR.drawable.ic_exclamation_mark

        @Composable
        internal fun getCounterIconColor(): Color = PassTheme.colors.interactionNormMinor2

        @Composable
        internal fun getCounterIconBackgroundColor(): Color = PassTheme.colors.passwordInteractionNormMajor2

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

            else -> PassTheme.colors.noteInteractionNormMinor2
        }

        @Composable
        @DrawableRes
        internal fun getCounterIcon(): Int = when (count) {
            null -> CompR.drawable.ic_checkmark
            0 -> CompR.drawable.ic_checkmark
            else -> CompR.drawable.ic_exclamation_mark
        }

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
        private val count: Int?,
        internal val showPassPlusIcon: Boolean
    ) : SecurityCenterCounterRowModel {

        override val counterText: String = count?.toString() ?: "-"

        override val isClickable: Boolean = if (showPassPlusIcon || count == null) {
            false
        } else {
            count > 0
        }

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
        @DrawableRes
        internal fun getCounterIcon(): Int = CompR.drawable.ic_checkmark

        @Composable
        internal fun getCounterIconColor(): Color = PassTheme.colors.interactionNormMinor2

        @Composable
        internal fun getCounterIconBackgroundColor(): Color = PassTheme.colors.cardInteractionNormMajor2

    }

    @Stable
    data class Loading(
        internal val title: String
    ) : SecurityCenterCounterRowModel {

        override val counterText: String = ""

        override val isClickable: Boolean = false
    }
}

internal class ThemeSecurityCenterCounterRowPreviewProvider :
    ThemePairPreviewProvider<SecurityCenterCounterRowModel>(
        SecurityCenterCounterRowPreviewProvider()
    )

@Preview
@Composable
internal fun SecurityCenterCounterRowPreview(
    @PreviewParameter(ThemeSecurityCenterCounterRowPreviewProvider::class)
    input: Pair<Boolean, SecurityCenterCounterRowModel>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SecurityCenterCounterRow(model = input.second)
        }
    }
}
