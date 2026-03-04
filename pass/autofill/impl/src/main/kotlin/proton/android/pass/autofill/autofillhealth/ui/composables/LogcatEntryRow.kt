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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import proton.android.pass.autofill.autofillhealth.model.LogcatEntry
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
internal fun LogcatEntryRow(entry: LogcatEntry) {
    val sourceColor = if (entry.isOwnProcess) ColorGreen else Color.Gray
    val sourceLabel = if (entry.isOwnProcess) "APP" else "SYS"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = sourceLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = sourceColor,
                modifier = Modifier
                    .background(
                        sourceColor.copy(alpha = 0.12f),
                        RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 3.dp)
            )
            if (entry.processName.isNotEmpty()) {
                Spacer(modifier = Modifier.width(Spacing.extraSmall))
                Text(
                    text = entry.processName,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(Spacing.extraSmall))
            Text(
                text = entry.timestamp.substringAfter(" "),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entry.level.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = colorForLogLevel(entry.level),
                modifier = Modifier
                    .background(
                        colorForLogLevel(entry.level).copy(alpha = 0.12f),
                        RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = Spacing.extraSmall)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = entry.tag,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = ColorBlue
            )
        }
        Text(
            text = entry.message,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = Spacing.extraSmall)
        )
    }
}

internal fun colorForLogLevel(level: Char): Color = when (level) {
    'V' -> Color.Gray
    'D' -> ColorBlue
    'I' -> ColorGreen
    'W' -> ColorOrange
    'E' -> ColorRed
    else -> Color.Gray
}

@Preview
@Composable
fun LogcatEntryRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            LogcatEntryRow(
                entry = LogcatEntry(
                    timestamp = "03-04 10:15:32.123",
                    level = 'D',
                    tag = "AutofillManager",
                    message = "Fill request received",
                    isOwnProcess = true
                )
            )
        }
    }
}
