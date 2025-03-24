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

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.text.PassTextWithLink
import proton.android.pass.features.security.center.R

@Composable
internal fun Footer(
    modifier: Modifier = Modifier,
    name: String,
    onOpenUrl: (String) -> Unit
) {
    PassTextWithLink(
        modifier = modifier,
        text = buildString {
            append(stringResource(id = R.string.security_center_report_detail_note, name))
            append(SpecialCharacters.SPACE)
            append(stringResource(R.string.security_center_report_detail_learn_more))
        },
        textStyle = PassTheme.typography.body3Norm()
            .copy(color = PassTheme.colors.textWeak),
        linkText = stringResource(R.string.security_center_report_detail_learn_more),
        linkStyle = PassTheme.typography.body3Norm()
            .copy(color = PassTheme.colors.interactionNormMajor2),
        onLinkClick = onOpenUrl,
        annotation = "https://proton.me/blog/breach-recommendations"
    )
}

@[Preview Composable]
internal fun FooterPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            Footer(name = "Breach site", onOpenUrl = {})
        }
    }
}
