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

package proton.android.pass.autofill.autofillhealth.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEvent
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEventType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
internal fun InfoCard(lastFillRequest: AutofillHealthEvent?, currentIme: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(Spacing.small)
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Text(
                text = "Last Fill Request",
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold
            )
            if (lastFillRequest != null) {
                Text(text = "Package: ${lastFillRequest.packageName ?: "unknown"}")
                Text(text = "Type: ${lastFillRequest.type.name}")
            } else {
                Text(text = "No fill requests recorded")
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = "Current IME",
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold
            )
            Text(text = currentIme.ifEmpty { "Unknown" })
        }
    }
}

@Preview
@Composable
fun InfoCardPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            InfoCard(
                lastFillRequest = AutofillHealthEvent(
                    timestamp = System.currentTimeMillis(),
                    type = AutofillHealthEventType.FILL_REQUEST_INLINE,
                    packageName = "com.example.app"
                ),
                currentIme = "Gboard"
            )
        }
    }
}
