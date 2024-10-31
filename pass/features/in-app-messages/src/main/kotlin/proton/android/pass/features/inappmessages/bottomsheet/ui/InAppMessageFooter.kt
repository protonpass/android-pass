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

package proton.android.pass.features.inappmessages.bottomsheet.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.notifications.api.InAppMessageCTARoute

@Composable
fun InAppMessageFooter(
    modifier: Modifier = Modifier,
    ctaText: String?,
    ctaRoute: InAppMessageCTARoute?,
    onCTAClick: (InAppMessageCTARoute) -> Unit
) {
    ctaRoute ?: return
    ctaText ?: return
    Button.Circular(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.mediumSmall),
        color = PassTheme.colors.interactionNormMajor2,
        onClick = { onCTAClick(ctaRoute) }
    ) {
        Text.Body1Regular(ctaText, color = PassTheme.colors.interactionNormMinor1)
    }
}
