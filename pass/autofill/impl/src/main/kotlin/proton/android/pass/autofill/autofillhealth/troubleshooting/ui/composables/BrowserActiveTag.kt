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

package proton.android.pass.autofill.autofillhealth.troubleshooting.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.text.Text

private const val BACKGROUND_ALPHA = 0.16f

@Composable
internal fun BrowserActiveTag(modifier: Modifier = Modifier) {
    Text.Body3Regular(
        modifier = modifier
            .background(
                color = PassTheme.colors.signalSuccess.copy(alpha = BACKGROUND_ALPHA),
                shape = CircleShape
            )
            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
        text = stringResource(R.string.autofill_troubleshooting_browser_active),
        color = PassTheme.colors.signalSuccess
    )
}

@Preview
@Composable
internal fun BrowserActiveTagPreview() {
    PassTheme {
        Surface {
            BrowserActiveTag()
        }
    }
}
