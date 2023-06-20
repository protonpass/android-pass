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

package proton.android.pass.featureitemdetail.impl.creditcard

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.HiddenContentRow
import proton.pass.domain.HiddenState
import proton.android.pass.composecomponents.impl.R as CompR

private const val CHAR_AMOUNT = 4

@Composable
fun CardCvvRow(
    modifier: Modifier = Modifier,
    cvv: HiddenState,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val (sectionContent, isContentVisible) = when (cvv) {
        is HiddenState.Concealed -> "•".repeat(CHAR_AMOUNT) to false
        is HiddenState.Revealed -> cvv.clearText to true
        is HiddenState.Empty -> "" to false
    }

    HiddenContentRow(
        modifier = modifier,
        sectionContent = AnnotatedString(sectionContent),
        label = stringResource(R.string.credit_card_cvv_field_name),
        isContentVisible = isContentVisible,
        toggleIconBackground = PassTheme.colors.cardInteractionNormMinor1,
        toggleIconForeground = PassTheme.colors.cardInteractionNormMajor2,
        icon = {
            Icon(
                painter = painterResource(CompR.drawable.ic_verified),
                contentDescription = null,
                tint = PassTheme.colors.cardInteractionNorm
            )
        },
        revealAction = stringResource(R.string.action_reveal_number),
        concealAction = stringResource(R.string.action_conceal_number),
        onToggleClick = onToggle,
        onRowClick = onClick
    )
}
