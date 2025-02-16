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
import proton.android.pass.domain.inappmessages.InAppMessageCTA
import proton.android.pass.domain.inappmessages.InAppMessageCTAType

@Composable
fun InAppMessageFooter(
    modifier: Modifier = Modifier,
    cta: InAppMessageCTA,
    onInternalCTAClick: (String) -> Unit,
    onExternalCTAClick: (String) -> Unit
) {
    Button.Circular(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.mediumSmall),
        color = PassTheme.colors.interactionNormMajor2,
        onClick = {
            when (cta.type) {
                InAppMessageCTAType.Internal -> onInternalCTAClick(cta.route)
                InAppMessageCTAType.External -> onExternalCTAClick(cta.route)
                InAppMessageCTAType.Unknown -> {}
            }
        }
    ) {
        Text.Body1Regular(cta.text, color = PassTheme.colors.interactionNormMinor1)
    }
}
