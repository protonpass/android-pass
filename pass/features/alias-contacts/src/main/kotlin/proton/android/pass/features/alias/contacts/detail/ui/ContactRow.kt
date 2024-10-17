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

package proton.android.pass.features.alias.contacts.detail.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.aliascontacts.Contact
import proton.android.pass.domain.aliascontacts.ContactId
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactUIEvent
import proton.android.pass.features.aliascontacts.R
import kotlin.time.DurationUnit
import me.proton.core.presentation.R as CoreR

@Composable
fun ContactRow(
    modifier: Modifier = Modifier,
    contact: Contact,
    onEvent: (DetailAliasContactUIEvent) -> Unit
) {
    Column(
        modifier = modifier
            .roundedContainerNorm()
            .padding(vertical = Spacing.small, horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text.Body1Regular(modifier = Modifier.weight(1f), text = contact.email)
            IconButton(onClick = { onEvent(DetailAliasContactUIEvent.SendEmail(contact.reverseAlias)) }) {
                Icon.Default(CoreR.drawable.ic_proton_paper_plane, tint = PassTheme.colors.textWeak)
            }
            IconButton(onClick = { onEvent(DetailAliasContactUIEvent.ContactOptions(contact.id)) }) {
                Icon.Default(
                    CoreR.drawable.ic_proton_three_dots_vertical,
                    tint = PassTheme.colors.textWeak
                )
            }
        }

        Column {
            val formattedDate = getTimeAgo(Instant.fromEpochMilliseconds(contact.createTime))
            Text.Body3Regular(
                stringResource(R.string.contact_created_date, formattedDate),
                color = PassTheme.colors.textWeak
            )
            val text = getActivitySummary(contact)
            Text.Body3Regular(text, color = PassTheme.colors.textWeak)
        }

        val (text, bgColor, border) = getButtonAttributes(contact.blocked)
        Button.Circular(
            color = bgColor,
            borderStroke = border,
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = {
                if (contact.blocked) {
                    onEvent(DetailAliasContactUIEvent.UnblockContact)
                } else {
                    onEvent(DetailAliasContactUIEvent.BlockContact)
                }
            }
        ) {
            Text.Body3Regular(
                text = text,
                color = PassTheme.colors.aliasInteractionNormMajor2
            )
        }
    }
}

@Composable
private fun getActivitySummary(contact: Contact): String = if (
    contact.blockedEmails ?: 0 > 0 ||
    contact.repliedEmails ?: 0 > 0 ||
    contact.forwardedEmails ?: 0 > 0
) {
    stringResource(
        R.string.activity_summary,
        contact.forwardedEmails ?: 0,
        contact.repliedEmails ?: 0,
        contact.blockedEmails ?: 0
    )
} else {
    stringResource(R.string.no_activity_contact)
}

@Composable
private fun getButtonAttributes(isBlocked: Boolean): Triple<String, Color, BorderStroke?> {
    val text = if (isBlocked) {
        stringResource(R.string.unblock_button)
    } else {
        stringResource(R.string.block_button)
    }
    val bgColor = if (isBlocked) {
        Color.Transparent
    } else {
        PassTheme.colors.aliasInteractionNormMinor1
    }
    val border = if (isBlocked) {
        BorderStroke(1.dp, SolidColor(PassTheme.colors.aliasInteractionNormMinor1))
    } else {
        null
    }
    return Triple(text, bgColor, border)
}

private const val MINUTES_IN_HOUR = 60
private const val HOURS_IN_DAY = 24
private const val DAYS_IN_WEEK = 7
private const val DAYS_IN_MONTH = 30
private const val DAYS_IN_YEAR = 365

@Composable
fun getTimeAgo(timestamp: Instant): String {
    val now = System.now()
    val duration = now - timestamp

    val minutes = duration.toDouble(DurationUnit.MINUTES).toInt()
    val hours = duration.toDouble(DurationUnit.HOURS).toInt()
    val days = duration.toDouble(DurationUnit.DAYS).toInt()

    return when {
        minutes < MINUTES_IN_HOUR -> pluralStringResource(R.plurals.minutes_ago, minutes, minutes)
        hours < HOURS_IN_DAY -> pluralStringResource(R.plurals.hours_ago, hours, hours)
        days < DAYS_IN_WEEK -> pluralStringResource(R.plurals.days_ago, days, days)
        days < DAYS_IN_MONTH -> pluralStringResource(
            R.plurals.weeks_ago,
            days / DAYS_IN_WEEK,
            days / DAYS_IN_WEEK
        )

        days < DAYS_IN_YEAR -> pluralStringResource(
            R.plurals.months_ago,
            days / DAYS_IN_MONTH,
            days / DAYS_IN_MONTH
        )

        else -> pluralStringResource(R.plurals.years_ago, days / DAYS_IN_YEAR, days / DAYS_IN_YEAR)
    }
}


@Preview
@Composable
fun ContactRowPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            ContactRow(
                contact = Contact(
                    id = ContactId(id = 0),
                    name = null,
                    blocked = input.second,
                    reverseAlias = "",
                    email = "contact@email",
                    createTime = 0,
                    repliedEmails = null,
                    forwardedEmails = null,
                    blockedEmails = null
                ),
                onEvent = {}
            )
        }
    }
}
