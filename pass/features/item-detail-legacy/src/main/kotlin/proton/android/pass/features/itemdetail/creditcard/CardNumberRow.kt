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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.SectionSubtitle
import me.proton.core.presentation.R as CoreR

@Composable
fun CardNumberRow(
    modifier: Modifier = Modifier,
    number: CardNumberState,
    isDowngradedMode: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    val icon = CoreR.drawable.ic_proton_credit_card
    val label = stringResource(R.string.credit_card_number_field_name)

    if (isDowngradedMode) {
        UpgradeRow(
            modifier = modifier,
            label = label,
            icon = icon,
            onUpgrade = onUpgradeClick
        )
    } else {
        val (sectionContent, actionIcon, actionContent) = when (number) {
            is CardNumberState.Masked -> CardNumberUIState(
                sectionContent = number.number,
                icon = CoreR.drawable.ic_proton_eye,
                actionContent = R.string.action_reveal_number
            )

            is CardNumberState.Visible -> CardNumberUIState(
                sectionContent = number.number,
                icon = CoreR.drawable.ic_proton_eye_slash,
                actionContent = R.string.action_conceal_number
            )
        }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = PassTheme.colors.cardInteractionNorm
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                SectionTitle(text = label)
                Spacer(modifier = Modifier.height(8.dp))
                SectionSubtitle(text = sectionContent.asAnnotatedString())
            }
            Circle(
                backgroundColor = PassTheme.colors.cardInteractionNormMinor1,
                onClick = { onToggle() }
            ) {
                Icon(
                    painter = painterResource(actionIcon),
                    contentDescription = stringResource(actionContent),
                    tint = PassTheme.colors.cardInteractionNormMajor2
                )
            }
        }
    }
}

private data class CardNumberUIState(
    val sectionContent: String,
    val icon: Int,
    val actionContent: Int
)

