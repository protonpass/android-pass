/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.upsell.v2.presentation.composables.welcomeOffer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.upsell.v1.R
import me.proton.core.presentation.compose.R as CoreR

@Composable
internal fun WhatsIncluded(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Text.Headline(
            modifier = Modifier.heightIn(min = 28.dp),
            text = stringResource(R.string.upsell_whats_included)
        )

        OneInclude(
            iconId = CoreR.drawable.ic_proton_alias,
            textId = R.string.upsell_plan_plus_welcome_1
        )

        OneInclude(
            iconId = CoreR.drawable.ic_proton_qr_code,
            textId = R.string.upsell_plan_plus_welcome_2
        )

        OneInclude(
            iconId = CoreR.drawable.ic_proton_users,
            textId = R.string.upsell_plan_plus_welcome_3
        )

        OneInclude(
            iconId = CoreR.drawable.ic_proton_credit_card,
            textId = R.string.upsell_plan_plus_welcome_4
        )

        OneInclude(
            iconId = CoreR.drawable.ic_proton_shield_2_bolt,
            textId = R.string.upsell_plan_plus_welcome_5
        )

        OneInclude(
            iconId = CoreR.drawable.ic_proton_file_lines,
            textId = R.string.upsell_plan_plus_welcome_6
        )
    }
}

@Composable
private fun OneInclude(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int,
    @StringRes textId: Int
) {
    Row(
        modifier = modifier.heightIn(min = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        Icon.Default(
            modifier = Modifier.size(18.dp),
            id = iconId
        )

        Text.Body2Regular(
            text = stringResource(textId)
        )
    }
}

@Preview
@Composable
fun WhatsIncludedPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            WhatsIncluded()
        }
    }
}
