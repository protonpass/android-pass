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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.presentation.R as CoreR
import proton.android.pass.autofill.autofillhealth.model.LogcatEntry
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.Circle

internal const val SETPROP_ADB_COMMANDS =
    "adb shell setprop log.tag.AutofillManager VERBOSE\n" +
        "adb shell setprop log.tag.AutofillManagerService VERBOSE\n" +
        "adb shell setprop log.tag.AutofillSession VERBOSE\n" +
        "adb shell setprop log.tag.AutofillUI VERBOSE\n" +
        "adb shell setprop log.tag.AutofillInlineSuggestionsRequestSession VERBOSE"

@Composable
internal fun LogcatTab(
    entries: List<LogcatEntry>,
    hasReadLogsPermission: Boolean,
    isVerbosePropsEnabled: Boolean,
    onClearLogcat: () -> Unit,
    onShareLogcat: () -> Unit
) {
    val packageName = LocalContext.current.packageName
    val clipboardManager = LocalClipboardManager.current
    LazyColumn(
        modifier = Modifier.padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
    ) {
        if (!hasReadLogsPermission) {
            item {
                Spacer(modifier = Modifier.height(Spacing.small))
                val readLogsCmd = "adb shell pm grant " +
                    "$packageName android.permission.READ_LOGS"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(Spacing.small),
                    backgroundColor = ColorOrange.copy(alpha = 0.15f)
                ) {
                    Column(modifier = Modifier.padding(Spacing.medium)) {
                        Text(
                            text = "READ_LOGS not granted",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            color = ColorOrange
                        )
                        Text(
                            text = "Only app-process logs visible. " +
                                "For system autofill logs, run once " +
                                "(persists until app reinstall):",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text(
                            text = readLogsCmd,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))
                        OutlinedButton(onClick = {
                            clipboardManager.setText(
                                AnnotatedString(readLogsCmd)
                            )
                        }) {
                            Text("Copy")
                        }
                    }
                }
            }
        }
        if (!isVerbosePropsEnabled) {
            item {
                Spacer(modifier = Modifier.height(Spacing.small))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(Spacing.small),
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(modifier = Modifier.padding(Spacing.medium)) {
                        Text(
                            text = "Verbose Logging disabled",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text(
                            text = "Run via ADB " +
                                "(resets on device reboot):",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text(
                            text = SETPROP_ADB_COMMANDS,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))
                        OutlinedButton(onClick = {
                            clipboardManager.setText(
                                AnnotatedString(SETPROP_ADB_COMMANDS)
                            )
                        }) {
                            Text("Copy")
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Logcat (${entries.size})",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Circle(
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onClick = onShareLogcat
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_arrow_up_from_square),
                            contentDescription = "Share",
                            tint = PassTheme.colors.interactionNormMajor2
                        )
                    }
                    Circle(
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onClick = onClearLogcat
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_trash),
                            contentDescription = "Clear",
                            tint = PassTheme.colors.interactionNormMajor2
                        )
                    }
                }
            }
        }
        items(entries) { entry ->
            LogcatEntryRow(entry = entry)
            Divider()
        }
        item {
            Spacer(modifier = Modifier.height(Spacing.medium))
        }
    }
}

@Preview
@Composable
fun LogcatTabPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            LogcatTab(
                entries = listOf(
                    LogcatEntry(
                        timestamp = "03-04 10:15:32.123",
                        level = 'D',
                        tag = "AutofillManager",
                        message = "Fill request received",
                        isOwnProcess = true
                    )
                ),
                hasReadLogsPermission = false,
                isVerbosePropsEnabled = false,
                onClearLogcat = {},
                onShareLogcat = {}
            )
        }
    }
}
