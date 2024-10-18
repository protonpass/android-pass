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

package proton.android.pass.features.alias.contacts.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultUnspecified
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.features.aliascontacts.R

@Composable
fun OnBoardingThirdPart(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        OnBoardingSeparation(counter = 3)
        val section3Text = buildAnnotatedString {
            append(stringResource(R.string.alias_contacts_bottomsheet_section_3))
            append(SpecialCharacters.SPACE)
            withStyle(
                style = ProtonTheme.typography.defaultUnspecified
                    .copy(PassTheme.colors.aliasInteractionNormMajor1)
                    .toSpanStyle()
            ) {
                append(stringResource(R.string.alias_contacts_bottomsheet_section_3_email))
            }
        }
        Text(
            text = section3Text,
            style = ProtonTheme.typography.defaultNorm
        )
        Image.Default(
            modifier = Modifier.fillMaxWidth(),
            id = R.drawable.recipient,
            contentScale = ContentScale.FillWidth
        )
    }
}

@Preview
@Composable
fun OnBoardingThirdPartPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            OnBoardingThirdPart()
        }
    }
}
