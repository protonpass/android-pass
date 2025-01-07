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

package proton.android.pass.features.itemdetail.creditcard

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.IconLabelTextRow
import me.proton.core.presentation.R as CoreR

@Composable
fun CardExpirationDateRow(modifier: Modifier = Modifier, expirationDate: String) {
    IconLabelTextRow(
        modifier = modifier,
        icon = {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_calendar_day),
                contentDescription = null,
                tint = PassTheme.colors.cardInteractionNorm
            )
        },
        label = stringResource(R.string.credit_card_expiration_date_field_name),
        content = expirationDate.asAnnotatedString()
    )

}

@Preview
@Composable
fun CardExpirationDateRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            CardExpirationDateRow(
                expirationDate = "12 / 2023"
            )
        }
    }
}
