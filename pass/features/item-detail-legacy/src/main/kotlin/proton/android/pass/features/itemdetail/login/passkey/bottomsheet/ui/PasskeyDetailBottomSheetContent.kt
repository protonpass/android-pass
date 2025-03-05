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

package proton.android.pass.features.itemdetail.login.passkey.bottomsheet.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.datetime.Instant
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.utils.passFormattedDateText
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyCreationData
import proton.android.pass.domain.PasskeyId
import proton.android.pass.features.itemdetail.R
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun PasskeyDetailBottomSheetContent(modifier: Modifier = Modifier, passkey: Passkey) {
    Column(
        modifier = modifier
            .bottomSheet()
            .padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        BottomSheetTitle(title = stringResource(R.string.passkey_detail_bottomsheet_title))

        Column(
            modifier = Modifier.roundedContainer(
                backgroundColor = PassTheme.colors.bottomSheetBackground,
                borderColor = PassTheme.colors.inputBorderNorm
            )
        ) {
            PasskeyDetailRow(
                title = stringResource(R.string.passkey_detail_bottomsheet_username),
                subtitle = passkey.userName,
                icon = CompR.drawable.ic_person_key
            )

            PassDivider()

            PasskeyDetailRow(
                title = stringResource(R.string.passkey_detail_bottomsheet_domain),
                subtitle = passkey.domain,
                icon = CoreR.drawable.ic_proton_earth
            )

            PassDivider()

            PasskeyDetailRow(
                title = stringResource(R.string.passkey_detail_bottomsheet_key),
                subtitle = passkey.id.value,
                icon = CoreR.drawable.ic_proton_key
            )

            PassDivider()

            PasskeyDetailRow(
                title = stringResource(R.string.passkey_detail_bottomsheet_created),
                subtitle = passFormattedDateText(endInstant = passkey.createTime),
                icon = CoreR.drawable.ic_proton_calendar_today
            )
        }
    }
}

@Composable
private fun PasskeyDetailRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    @DrawableRes icon: Int
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = PassTheme.colors.loginInteractionNormMajor2
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
        ) {
            SectionTitle(text = title)

            SectionSubtitle(text = subtitle.asAnnotatedString())
        }
    }
}

@Preview
@Composable
@Suppress("MagicNumber")
internal fun PasskeyDetailBottomSheetContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    val now = Instant.fromEpochSeconds(1_708_677_937)

    PassTheme(isDark = isDark) {
        Surface {
            PasskeyDetailBottomSheetContent(
                passkey = Passkey(
                    id = PasskeyId("A1B2C3D4"),
                    domain = "test.domain",
                    rpId = "rpId",
                    rpName = "rpName",
                    userName = "userName",
                    userDisplayName = "User name",
                    userId = ByteArrayWrapper(byteArrayOf()),
                    note = "Note",
                    createTime = now,
                    contents = ByteArrayWrapper(byteArrayOf()),
                    userHandle = null,
                    credentialId = ByteArrayWrapper(byteArrayOf()),
                    creationData = PasskeyCreationData(
                        osName = "Android",
                        osVersion = "14",
                        appVersion = "1.2.3",
                        deviceName = "Pixel 6"
                    )
                )
            )
        }
    }
}
