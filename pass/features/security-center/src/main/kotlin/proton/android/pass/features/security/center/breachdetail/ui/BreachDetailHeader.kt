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

package proton.android.pass.features.security.center.breachdetail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.shared.ui.DateUtils
import proton.android.pass.features.security.center.shared.ui.image.BreachImage

@Composable
internal fun BreachDetailHeader(modifier: Modifier = Modifier, breachEmail: BreachEmail) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BreachImage(isResolved = breachEmail.isResolved)

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Text(text = breachEmail.name, style = ProtonTheme.typography.headline)

            DateUtils.formatDate(breachEmail.publishedAt)
                .onSuccess { date ->
                    val bodyTextResource =
                        stringResource(R.string.security_center_report_detail_subtitle)
                    val bodyText = buildAnnotatedString {
                        val textParts = bodyTextResource.split("__DATE__")
                        if (textParts.size == 2) {
                            append(textParts[0])
                            append(
                                AnnotatedString(
                                    date,
                                    SpanStyle(fontWeight = FontWeight.Bold)
                                )
                            )
                            append(textParts[1])
                        } else {
                            append(bodyTextResource)
                        }
                    }

                    Text(
                        text = bodyText,
                        style = PassTheme.typography.body3Weak()
                    )
                }
        }
    }
}
