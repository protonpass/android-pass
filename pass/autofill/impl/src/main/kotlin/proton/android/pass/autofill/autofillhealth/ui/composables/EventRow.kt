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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEvent
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEventType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun EventRow(event: AutofillHealthEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatTimestamp(event.timestamp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(90.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.small))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.type.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colorForEventType(event.type)
            )
            event.packageName?.let { pkg ->
                Text(
                    text = pkg,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

internal fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

internal fun colorForEventType(type: AutofillHealthEventType): Color = when (type) {
    AutofillHealthEventType.CREATED -> Color.Gray
    AutofillHealthEventType.CONNECTED -> ColorGreen
    AutofillHealthEventType.DISCONNECTED -> ColorRed
    AutofillHealthEventType.FILL_REQUEST_INLINE -> ColorBlue
    AutofillHealthEventType.FILL_REQUEST_MENU -> ColorOrange
    AutofillHealthEventType.FILL_REQUEST_NONE -> Color.Gray
    AutofillHealthEventType.FILL_REQUEST_ERROR -> ColorRed
}

@Preview
@Composable
fun EventRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            EventRow(
                event = AutofillHealthEvent(
                    timestamp = 1_700_000_000_000L,
                    type = AutofillHealthEventType.FILL_REQUEST_INLINE,
                    packageName = "com.example.app"
                )
            )
        }
    }
}
