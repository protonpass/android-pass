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

package proton.android.pass.features.itemdetail.login.customfield

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.login.CustomFieldUiContent
import me.proton.core.presentation.compose.R as CoreR

@Composable
fun CustomFieldLimited(
    modifier: Modifier = Modifier,
    entry: CustomFieldUiContent.Limited,
    onUpgrade: () -> Unit
) {
    val (icon, description) = when (entry) {
        is CustomFieldUiContent.Limited.Hidden -> {
            CoreR.drawable.ic_proton_eye_slash to R.string.custom_field_hidden_icon_description
        }
        is CustomFieldUiContent.Limited.Text -> {
            CoreR.drawable.ic_proton_text_align_left to R.string.custom_field_text_icon_description
        }
        is CustomFieldUiContent.Limited.Totp -> {
            CoreR.drawable.ic_proton_lock to R.string.custom_field_totp_icon_description
        }
    }

    RoundedCornersColumn(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(
                start = Spacing.medium,
                top = Spacing.mediumSmall,
                end = Spacing.mediumSmall,
                bottom = Spacing.mediumSmall
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = stringResource(description),
                tint = PassTheme.colors.loginInteractionNorm
            )
            Column(modifier = Modifier.padding(start = Spacing.small)) {
                SectionTitle(
                    modifier = Modifier.padding(start = Spacing.small),
                    text = entry.label
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onUpgrade)
                        .padding(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    Text(
                        text = stringResource(proton.android.pass.composecomponents.impl.R.string.upgrade),
                        style = ProtonTheme.typography.defaultNorm,
                        color = PassTheme.colors.interactionNormMajor2
                    )
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(CoreR.drawable.ic_proton_arrow_out_square),
                        contentDescription = null,
                        tint = PassTheme.colors.interactionNormMajor2
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CustomFieldLimitedPreview(
    @PreviewParameter(ThemedCFLimitedProvider::class) input: Pair<Boolean, CustomFieldUiContent.Limited>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CustomFieldLimited(
                entry = input.second,
                onUpgrade = {}
            )
        }
    }
}
