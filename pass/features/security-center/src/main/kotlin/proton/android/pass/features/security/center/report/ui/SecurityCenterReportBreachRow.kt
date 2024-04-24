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

package proton.android.pass.features.security.center.report.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportUiEvent.EmailBreachDetail
import proton.android.pass.features.security.center.shared.ui.image.BreachImage
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

@Composable
internal fun SecurityCenterReportBreachRow(
    modifier: Modifier = Modifier,
    breach: BreachEmail,
    onUiEvent: (SecurityCenterReportUiEvent) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onUiEvent(EmailBreachDetail(breach.emailId)) }
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        BreachImage(isResolved = breach.isResolved)

        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = breach.name,
                style = ProtonTheme.typography.defaultNorm,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            val formattedDate = runCatching {
                val date = DateTimeFormatter.ISO_DATE_TIME.parse(breach.publishedAt)
                val dateFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(date)
            }.getOrNull()
            formattedDate?.let {
                Text(
                    text = it,
                    style = ProtonTheme.typography.defaultSmallWeak,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}

class BreachRowPreviewProvider : PreviewParameterProvider<BreachEmail> {

    override val values: Sequence<BreachEmail>
        get() = sequenceOf(
            createBreach(
                name = "breach 1",
                severity = 0.1,
                publishedAt = "2022-11-02T00:00:00+00:00",
                isResolved = false
            ),
            createBreach(
                name = "breach 2",
                severity = 0.7,
                publishedAt = "",
                isResolved = false
            ),
            createBreach(
                name = "breach 1",
                severity = 0.1,
                publishedAt = "2022-11-02T00:00:00+00:00",
                isResolved = true
            ),
            createBreach(
                name = "breach 2",
                severity = 0.7,
                publishedAt = "",
                isResolved = true
            )
        )

    private fun createBreach(
        name: String,
        severity: Double,
        publishedAt: String,
        isResolved: Boolean
    ) = BreachEmail(
        emailId = BreachEmailId.Custom(BreachId(Random.nextInt().toString())),
        email = "",
        severity = severity,
        name = name,
        createdAt = "",
        publishedAt = publishedAt,
        size = 0,
        passwordLastChars = "",
        exposedData = emptyList(),
        isResolved = isResolved
    )
}

class ThemedBreachRowPreviewProvider : ThemePairPreviewProvider<BreachEmail>(
    provider = BreachRowPreviewProvider()
)

@Preview
@Composable
fun BreachRowPreview(@PreviewParameter(ThemedBreachRowPreviewProvider::class) input: Pair<Boolean, BreachEmail>) {
    PassTheme(isDark = input.first) {
        Surface {
            SecurityCenterReportBreachRow(
                breach = input.second,
                onUiEvent = {}
            )
        }
    }
}
