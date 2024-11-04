/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featurehome.impl.empty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headlineSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.featurehome.impl.R
import proton.android.pass.searchoptions.api.SearchFilterType

@Composable
fun HomeEmptyHeader(modifier: Modifier = Modifier, filterType: SearchFilterType) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val (title, desc) = remember(filterType) {
            when (filterType) {
                SearchFilterType.All ->
                    R.string.home_empty_vault_title to
                        R.string.home_empty_vault_subtitle
                SearchFilterType.Login ->
                    R.string.home_empty_vault_login_title to
                        R.string.home_empty_vault_login_subtitle
                SearchFilterType.LoginMFA ->
                    R.string.home_empty_vault_logins_mfa_title to
                        R.string.home_empty_vault_logins_mfa_subtitle
                SearchFilterType.Alias ->
                    R.string.home_empty_vault_aliases_title to
                        R.string.home_empty_vault_aliases_subtitle
                SearchFilterType.Note ->
                    R.string.home_empty_vault_notes_title to
                        R.string.home_empty_vault_notes_subtitle
                SearchFilterType.CreditCard ->
                    R.string.home_empty_vault_cc_title to
                        R.string.home_empty_vault_cc_subtitle
                SearchFilterType.Identity ->
                    R.string.home_empty_vault_identity_title to
                        R.string.home_empty_vault_identity_subtitle
            }
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(title),
            style = ProtonTheme.typography.headlineSmallNorm,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(desc),
            style = PassTheme.typography.body3Norm(),
            color = PassTheme.colors.textWeak,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun HomeEmptyHeaderPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            HomeEmptyHeader(filterType = SearchFilterType.All)
        }
    }
}
