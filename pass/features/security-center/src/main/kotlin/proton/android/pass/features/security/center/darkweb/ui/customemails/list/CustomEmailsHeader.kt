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

package proton.android.pass.features.security.center.darkweb.ui.customemails.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.features.security.center.R

@Composable
internal fun CustomEmailsHeader(
    modifier: Modifier = Modifier,
    canAddCustomEmails: Boolean,
    onAddClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.security_center_dark_web_monitor_custom_emails_title),
            style = ProtonTheme.typography.body2Regular
        )

        val (background, foreground) = if (canAddCustomEmails) {
            PassTheme.colors.interactionNormMinor1 to PassTheme.colors.interactionNormMajor2
        } else {
            PassTheme.colors.interactionNormMinor1.copy(alpha = DISABLED_ALPHA) to
                PassTheme.colors.interactionNormMajor2.copy(alpha = DISABLED_ALPHA)
        }

        Circle(
            backgroundColor = background,
            onClick = onAddClick
        ) {
            Icon(
                modifier = Modifier.padding(all = Spacing.medium.minus(Spacing.extraSmall)),
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_plus),
                contentDescription = stringResource(
                    id = R.string.security_center_dark_web_monitor_custom_emails_add_content_description
                ),
                tint = foreground
            )
        }

    }
}

@Preview
@Composable
fun CustomEmailsHeaderPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            CustomEmailsHeader(
                canAddCustomEmails = input.second,
                onAddClick = {}
            )
        }
    }
}

private const val DISABLED_ALPHA = 0.3f
